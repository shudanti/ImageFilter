package com.example.saturn.imagefilter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class GLLayer extends GLSurfaceView implements GLSurfaceView.Renderer {
	/**
	 * This class implements our custom renderer. Note that the GL10 parameter
	 * passed in is unused for OpenGL ES 2.0 renderers -- the static class
	 * GLES20 is used instead.
	 */
	private final Context mActivityContext;

	/**
	 * Store the model matrix. This matrix is used to move models from object
	 * space (where each model can be thought of being located at the center of
	 * the universe) to world space.
	 */
	private float[] mModelMatrix = new float[16];

	/**
	 * Store the view matrix. This can be thought of as our camera. This matrix
	 * transforms world space to eye space; it positions things relative to our
	 * eye.
	 */
	private float[] mViewMatrix = new float[16];

	/**
	 * Store the projection matrix. This is used to project the scene onto a 2D
	 * viewport.
	 */
	private float[] mProjectionMatrix = new float[16];

	/**
	 * Allocate storage for the final combined matrix. This will be passed into
	 * the shader program.
	 */
	private float[] mMVPMatrix = new float[16];

	/** Store our model data in a float buffer. */
	private final FloatBuffer mCubePositions;
	private final FloatBuffer mCubeColors;
	private final FloatBuffer mCubeTextureCoordinates;

	/** This will be used to pass in the transformation matrix. */
	private int mMVPMatrixHandle;

	/** This will be used to pass in the texture. */
	private int mTextureUniformHandle0;
	private int mTextureUniformHandle1;

	/** This will be used to pass in model position information. */
	private int mPositionHandle;

	/** This will be used to pass in model color information. */
	// private int mColorHandle;

	/** This will be used to pass in model texture coordinate information. */
	private int mTextureCoordinateHandle;

	/** How many bytes per float. */
	private final int mBytesPerFloat = 4;

	/** Size of the position data in elements. */
	private final int mPositionDataSize = 3;

	/** Size of the color data in elements. */
	// private final int mColorDataSize = 4;

	/** Size of the texture coordinate data in elements. */
	private final int mTextureCoordinateDataSize = 2;

	/** This is a handle to our cube shading program. */
	private int mProgramHandle;

	/** This is a handle to our texture data. */
	static public int mTextureDataHandle0;
	static public int mTextureDataHandle1;
	
	/**
	 * Shader Titles
	 */
	static public int shader_selection = 0;
	static public int old_shader_selection = -1;
	static public final int BLUR = 1;
	static public final int EDGE = 2;
	static public final int EMBOSS = 3;
	static public final int FILTER = 4;
	static public final int FLIP = 5;
	static public final int HUE = 6;
	static public final int LUM = 7;
	static public final int NEG = 8;
	static public final int TOON = 9;
	static public final int TWIRL = 10;
	static public final int WARP = 11;
	//and more ...

	private Bitmap bitmap;
	static private int orientation;
	static private String bitmapPath;

	/* Rotation values */
	static private float xrot;                 //X Rotation
	static private float yrot;                 //Y Rotation

	/* Rotation speed values */

	static private float xspeed;               //X Rotation Speed ( NEW )
	static private float yspeed;               //Y Rotation Speed ( NEW )

	static private float z = 320.0f;

	static private float oldX;
	static private float oldY;
	private final float TOUCH_SCALE = 0.8f;     //Proved to be good for normal rotation ( NEW )
	private ScaleGestureDetector mScaleDetector;
	static private int vWidth;
	static private int vHeight;
	/**
	 * Initialize the model data.
	 */
	public GLLayer(final Context activityContext, Bitmap bmp, Uri uriImage) {
		super(activityContext);

		mActivityContext = activityContext;
		bitmap = bmp;
		bitmapPath = getRealPathFromURI(uriImage);
		xrot = 0;
		yrot = 0;
		oldX = 0;
		oldY = 0;
		sizeCoef = 1;
		old_shader_selection = -1;
		mScaleDetector = new ScaleGestureDetector(activityContext, new ScaleDetectorListener());
		// Define points for a cube.

		// X, Y, Z
		final float[] cubePositionData = {
				// In OpenGL counter-clockwise winding is default. This means
				// that when we look at a triangle,
				// if the points are counter-clockwise we are looking at the
				// "front". If not we are looking at
				// the back. OpenGL has an optimization where all back-facing
				// triangles are culled, since they
				// usually represent the backside of an object and aren't
				// visible anyways.

				// Front face
				-1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
				-1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f };

		// R, G, B, A
		final float[] cubeColorData = {
				// Front face (red)
				1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
				1.0f, 0.0f, 0.0f, 1.0f };

		// X, Y, Z
		// The normal is used in light calculations and is a vector which points
		// orthogonal to the plane of the surface. For a cube model, the normals
		// should be orthogonal to the points of each face.

		// S, T (or X, Y)
		// Texture coordinate data.
		// Because images have a Y axis pointing downward (values increase as
		// you move down the image) while
		// OpenGL has a Y axis pointing upward, we adjust for that here by
		// flipping the Y axis.
		// What's more is that the texture coordinates are the same for every
		// face.
		final float[] cubeTextureCoordinateData = {
				// Front face
				0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
				1.0f, 0.0f };

		// Initialize the buffers.
		mCubePositions = ByteBuffer
				.allocateDirect(cubePositionData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mCubePositions.put(cubePositionData).position(0);

		mCubeColors = ByteBuffer
				.allocateDirect(cubeColorData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mCubeColors.put(cubeColorData).position(0);

		mCubeTextureCoordinates = ByteBuffer
				.allocateDirect(
						cubeTextureCoordinateData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);
	}

	protected String getVertexShader() {
		return RawResourceReader.readTextFileFromRawResource(mActivityContext,
				R.raw._vertex_shader);
	}


	protected String getFragmentShader() {
		int id;
		switch (shader_selection){
			case BLUR: id = R.raw.blurring_fragment_shader; break;
			case EDGE: id = R.raw.edge_detect_fragment_shader;break;
			case EMBOSS: id = R.raw.emboss_fragment_shader;break;
			case FILTER: id = R.raw.filter_fragment_shader;break;
			case FLIP: id = R.raw.flip_fragment_shader;break;
			case HUE: id = R.raw.hueshift_fragment_shader;break;
			case LUM: id = R.raw.luminance_fragment_shader;break;
			case NEG: id = R.raw.negative_fragment_shader;break;
			case TOON: id = R.raw.toon_fragment_shader;break;
			case TWIRL: id = R.raw.twirl_fragment_shader;break;
			case WARP: id = R.raw.warp_fragment_shader;break;
			default: id = R.raw._fragment_shader;break;
		}



		return RawResourceReader.readTextFileFromRawResource(mActivityContext, id);
	}

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
		// Set the background clear color to black.
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		// Use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);

		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		// The below glEnable() call is a holdover from OpenGL ES 1, and is not
		// needed in OpenGL ES 2.
		// Enable texture mapping
		// GLES20.glEnable(GLES20.GL_TEXTURE_2D);

		// Position the eye in front of the origin.
		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = -0.5f;

		// We are looking toward the distance
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = -5.0f;

		// Set our up vector. This is where our head would be pointing were we
		// holding the camera.
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;

		// Set the view matrix. This matrix can be said to represent the camera
		// position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination
		// of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices
		// separately if we choose.
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY,
				lookZ, upX, upY, upZ);


		if (bitmap == null) {
			// Load the texture
			mTextureDataHandle0 = TextureHelper.loadTexture(mActivityContext,
					R.drawable.image);

			// Load the texture
			mTextureDataHandle1 = TextureHelper.loadTexture(mActivityContext,
					R.drawable.image);
		} else {
			GLLayer.changeTexture(0, bitmap);
			GLLayer.changeTexture(1, bitmap);
		}

	}

	static private int realWidth;
	static private int realHeight;
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);
		vWidth = width;
		vHeight = height;
		// Create a new perspective projection matrix. The height will stay the
		// same
		// while the width will vary as per aspect ratio.
		final float ratio = (float)imageHeight/imageWidth;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1/ratio;
		final float top = 1/ratio;
		final float near = -1;
		final float far = 10;

		float imgAspectRatio = width / (float)height;
		float viewAspectRatio =  (float)imageWidth/imageHeight;
		if(orientation == ExifInterface.ORIENTATION_ROTATE_90)
		{
			imgAspectRatio = 1/imgAspectRatio;
			viewAspectRatio = 1/viewAspectRatio;
		}
		float relativeAspectRatio = viewAspectRatio / imgAspectRatio;

		float x0, y0, x1, y1;
		if(orientation == ExifInterface.ORIENTATION_ROTATE_90) {
			if (relativeAspectRatio > 1.0f) {
				y0 = -1.0f;
				x0 = -relativeAspectRatio;
				y1 = 1.0f;
				x1 = relativeAspectRatio;
				realHeight =  height;
				realWidth = imageWidth*realHeight/imageHeight;
			} else {
				y0 = -1.0f / relativeAspectRatio;
				x0 = -1.0f;
				y1 = 1.0f / relativeAspectRatio;
				x1 = 1.0f;
				realWidth = width;
				realHeight =  imageHeight*realWidth/imageWidth;
			}

			Matrix.orthoM(mProjectionMatrix, 0, x0, x1, y0, y1, near, far);
			Matrix.rotateM(mProjectionMatrix, 0, -90, 0, 0, 1.0f);
		} else {
			if (relativeAspectRatio > 1.0f) {
				x0 = -1.0f;
				y0 = -relativeAspectRatio;
				x1 = 1.0f;
				y1 = relativeAspectRatio;
				realWidth = width;
				realHeight =  imageHeight*realWidth/imageWidth;

			} else {
				x0 = -1.0f / relativeAspectRatio;
				y0 = -1.0f;
				x1 = 1.0f / relativeAspectRatio;
				y1 = 1.0f;
				realHeight =  height;
				realWidth = imageWidth*realHeight/imageHeight;
			}
			Matrix.orthoM(mProjectionMatrix, 0, x0, x1, y0, y1, near, far);
		}

	}

	private static GL10 _glUnused;

	@Override
	public void onDrawFrame(GL10 glUnused) {
		_glUnused = glUnused;
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		final String vertexShader = getVertexShader();
		final String fragmentShader = getFragmentShader();

		final int vertexShaderHandle = ShaderHelper.compileShader(
				GLES20.GL_VERTEX_SHADER, vertexShader);
		final int fragmentShaderHandle = ShaderHelper.compileShader(
				GLES20.GL_FRAGMENT_SHADER, fragmentShader);

		mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle,
				fragmentShaderHandle, new String[] { "a_Position",
						"a_TexCoordinate" });

		// Set our per-vertex lighting program.
		GLES20.glUseProgram(mProgramHandle);

		// Set program handles for cube drawing.
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle,
				"u_MVPMatrix");
		mTextureUniformHandle0 = GLES20.glGetUniformLocation(mProgramHandle,
				"u_Texture0");
		mTextureUniformHandle1 = GLES20.glGetUniformLocation(mProgramHandle,
				"u_Texture1");
		mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle,
				"a_Position");
		mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle,
				"a_TexCoordinate");

		/**
		 * First texture map
		 */
		// Set the active texture0 unit to texture unit 0.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

		// Bind the texture to this unit.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle0);

		// Tell the texture uniform sampler to use this texture in the shader by
		// binding to texture unit 0.
		GLES20.glUniform1i(mTextureUniformHandle0, 0);
		
		/**
		 * Second texture map
		 */
		// Set the active texture1 unit to texture unit 1.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);

		// Bind the texture to this unit.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle1);

		// Tell the texture uniform sampler to use this texture in the shader by
		// binding to texture unit 1.
		GLES20.glUniform1i(mTextureUniformHandle1, 1);

		// Draw some cubes.
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -3.2f);
		Matrix.rotateM(mModelMatrix, 0, 0.0f, 1.0f, 1.0f, 0.0f);
		//Matrix.rotateM(mModelMatrix, 0, xrot, 1.0f, 0f, 1.0f);
		//Matrix.rotateM(mModelMatrix, 0, yrot, 0f, 1.0f, 1.0f);
		if(sizeCoef >1)
		{
			Matrix.translateM(mModelMatrix, 0, yrot/200, -xrot/200, -3.2f);
		}
		Matrix.scaleM(mModelMatrix, 0, sizeCoef, sizeCoef, 0);
		drawCube();

		if(old_shader_selection != shader_selection)
		{
			count = 0;
			old_shader_selection = shader_selection;
			xrot = 0;
			yrot = 0;
			oldX = 0;
			oldY = 0;
			sizeCoef = 1;
		}
		if(count == 2)
		{
			saveBitmap = takeScreenshot();
		}
		xrot += xspeed;
		yrot += yspeed;
		count++;
	}
	int count = 0;
	/**
	 * Draws a cube.
	 */
	private void drawCube() {
		// Pass in the position information
		mCubePositions.position(0);
		GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize,
				GLES20.GL_FLOAT, false, 0, mCubePositions);

		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// Pass in the texture coordinate information
		mCubeTextureCoordinates.position(0);
		GLES20.glVertexAttribPointer(mTextureCoordinateHandle,
				mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0,
				mCubeTextureCoordinates);

		GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

		// This multiplies the view matrix by the model matrix, and stores the
		// result in the MVP matrix
		// (which currently contains model * view).
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

		// This multiplies the modelview matrix by the projection matrix, and
		// stores the result in the MVP matrix
		// (which now contains model * view * projection).
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

		// Pass in the combined matrix.
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

		// Draw the cube.
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
	}

	static int imageWidth = 400;
	static int imageHeight = 250;
	public static void changeTexture(int i, Bitmap bmp)
	{
		if(bitmapPath == null)
		{
			orientation = ExifInterface.ORIENTATION_NORMAL;
		}
		else {
			orientation = getExifOrientation();
		}
		if(orientation == ExifInterface.ORIENTATION_ROTATE_90)
		{
			imageHeight = bmp.getWidth();
			imageWidth= bmp.getHeight();
		}
		else {
			imageWidth = bmp.getWidth();
			imageHeight = bmp.getHeight();
		}
		if(i == 0) {
			mTextureDataHandle0 = TextureHelper.loadTextureBitmap(bmp);
		} else if(i == 1) {
			mTextureDataHandle1 = TextureHelper.loadTextureBitmap(bmp);
		}
	}
	private  String getRealPathFromURI(Uri contentUri) {
		if(contentUri == null)
		{
			return null;
		}
		String[] proj = { MediaStore.Images.Media.DATA };
		CursorLoader loader = new CursorLoader(mActivityContext, contentUri, proj, null, null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	private static int getExifOrientation() {
		ExifInterface exif;
		orientation = 0;
		try {
			exif = new ExifInterface(bitmapPath);
			orientation = exif.getAttributeInt( ExifInterface.TAG_ORIENTATION, 1 );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		return orientation;
	}
	public Bitmap takeScreenshot()
	{
		final int mWidth = realWidth;
		final int mHeight = realHeight;
		IntBuffer ib = IntBuffer.allocate(mWidth * mHeight);
		IntBuffer ibt = IntBuffer.allocate(mWidth * mHeight);

		_glUnused.glReadPixels(vWidth/2 - realWidth/2, vHeight/2 - realHeight/2, mWidth, mHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);

		// Convert upside down mirror-reversed image to right-side up normal
		// image.
		for (int i = 0; i < mHeight; i++) {
			for (int j = 0; j < mWidth; j++) {
				ibt.put((mHeight - i - 1) * mWidth + j, ib.get(i * mWidth + j));
			}
		}
		Bitmap mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);

		mBitmap.copyPixelsFromBuffer(ibt);
		return mBitmap;
	}

	public static Bitmap saveBitmap;
	public Bitmap getBitmap()
	{
		return saveBitmap;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mScaleDetector.onTouchEvent(event);
		//
		float x = event.getX();
		float y = event.getY();

		//If a touch is moved on the screen
		if(event.getAction() == MotionEvent.ACTION_MOVE && !isScale) {
			//Calculate the change
			float dx = x - oldX;
			float dy = y - oldY;

			if(Math.abs(dx)  > 100 || Math.abs(dy) > 100)
			{
				return false;
			}
			//Define an upper area of 10% on the screen
			int upperArea = imageHeight / 10;

			//Zoom in/out if the touch move has been made in the upper
			if(y < upperArea) {
				z -= dx * TOUCH_SCALE / 2;

				//Rotate around the axis otherwise
			} else {
				xrot += dy * TOUCH_SCALE;
				yrot += dx * TOUCH_SCALE;
			}

			//A press on the screen
		} else if(event.getAction() == MotionEvent.ACTION_UP) {


		}

		//Remember the values
		oldX = x;
		oldY = y;

		//We handled the event
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		//
		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

		} else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {

		} else if(keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			z -= 3;

		} else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			z += 3;

		} else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {

		}

		//We handled the event
		return true;
	}
	static private float sizeCoef = 1;
	static private boolean isScale = false;

	private class ScaleDetectorListener implements ScaleGestureDetector.OnScaleGestureListener{

		float scaleFocusX = 0;
		float scaleFocusY = 0;

		public boolean onScale(ScaleGestureDetector arg0) {
			float scale = arg0.getScaleFactor() * sizeCoef;

			sizeCoef = scale;

			requestRender();

			return true;
		}

		public boolean onScaleBegin(ScaleGestureDetector arg0) {
			invalidate();
			isScale = true;
			scaleFocusX = arg0.getFocusX();
			scaleFocusY = arg0.getFocusY();

			return true;
		}

		public void onScaleEnd(ScaleGestureDetector arg0) {
			scaleFocusX = 0;
			scaleFocusY = 0;
			isScale = false;
		}
	}

}
