package com.aromasoft;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.aromasoft.util.Image;

import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class CubeRenderer implements Renderer
{
    private final View mView;


    private ArrayList<Cube> cubes;
    private int [] mTextureID = null;

    private HashMap<Integer, Float> positionMap;
    private int mCubeCount;

    public float mX;
    public float DISTANCE = 2.0f;
    public float CRITERIA = 2.0f;

    public int ROTATE_ANGLE = 30;
    public float RIGHT_MAX_ROTATE_ANGLE = -60f;
    public float LEFT_MAX_ROTATE_ANGLE = 60f;

    public float MOVE_EXACTLY_VALUE = 5f;
    public float MOVE_MORE_EXACTLY_VALUE = 1f;

    public float MAX_REM_VALUE = 195f;
    public float MIN_REM_VALUE = 4.5f;

    public final int COMPENSATION_VALUE = 100;

    public float SCALE = 0.7f;
    public float SCALE_COMPENSATION_VALUE = CRITERIA * SCALE * 100;

    public boolean mIsMoving = false;

    public int selectedPosition;

    private Context mContext;

    public CubeRenderer(CubeSurfaceView view, Context context, int cubeCount)
    {
        mContext = context;
        mView = view;
        mX = 0;
        mCubeCount = cubeCount;
        mTextureID = new int[mCubeCount];

        cubes = new ArrayList<Cube>();
        for (int i = 0; i < mCubeCount; i++)
        {
            cubes.add(new Cube());
        }

        if (positionMap == null)
        {
            positionMap = new HashMap<Integer, Float>();
        }

        positionMap.clear();
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        gl.glGenTextures(mCubeCount, mTextureID, 0);

        for( int i = 0; i < mCubeCount ; i++ )
        {
            Bitmap bitmap = Image.loadImageByMp3(mContext, mView, i);

            if (bitmap != null)
            {
                loadTexture(gl, bitmap, i);
            }
        }

        gl.glEnable(GL10.GL_TEXTURE_2D);
        // 물체를 부드럽게 처리해준다.
        // 둘중 하나의 인자를 가진다. GL_FLAT or GL_SMOOTH
        gl.glShadeModel(GL10.GL_SMOOTH);
        // 화면을 설정된 값으로 지운다.(또는 채워서 칠한다.)
        // red, green, blue, alpha 이며 0.0f~ 1.0f 로  어두운값에서 밝은 값으로 정의된다.
        gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );

        // 깊이 버퍼를 설정한다. 물체가  여러개 있을때 앞에 있는지 뒤에 있는지를 구분하여 표현되어 진다.
        gl.glClearDepthf(1.0f);
        // 깊이 테스팅을 활성화 해 줍니다.
        gl.glEnable( GL10.GL_DEPTH_TEST );
        // 깊이 버퍼 종류가 해야할 것을 설정한다.
        gl.glDepthFunc( GL10.GL_LEQUAL );

        // 가장 빠른 화면을 그리도록 요청한다.
        gl.glHint( GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST );
    }

    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        if (height == 0)
        {
            height = 1;
        }

        // 그려질 화면을 전체 화면으로 설정한다.
        gl.glViewport(0, 0, width, height);

        // Projection View Matrix 에 영향을 미칠 값이 있다고 알린다.
        // Projection View Perspective View 에 영향을 미치는 Matrix 이다.
        gl.glMatrixMode(GL10.GL_PROJECTION);
        // Matrix 를 초기화한다. ( Matrix 를 초기상태인 단위 행렬로 만든다. )
        gl.glLoadIdentity();

        // 관측 윈도우의 각도와 표현할 near & far 의 깊이를 설정한다.
        GLU.gluPerspective(gl, 45.0f, (float) width / height, 1.0f, 100.0f);

        // Model View Matrix 에 영향을 미칠 값이 있다고 알린다.
        // Model View Matrix 를 물체의 정보가 저장되는 곳이다.
        // 물체의 정보라 하면 3차원적 위치, 3차원 크기, 3차원적 회전각을 의미하며,
        // 이모든것은 4x4 행렬로 표현된다.
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        // Model View Matrix 를 초기화한다.
        gl.glLoadIdentity();
    }

    public void onDrawFrame(GL10 gl)
    {
        int center = getCenterPosition();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        if (!mIsMoving)
        {
            if (isNeedAutoLock())
            {
                moveToExactlyItemPosition();
            }
            else
            {
                Intent intent = new Intent(CubeActivity.EXACTLYPOSITION);
                intent.putExtra("position", selectedPosition);
                mContext.sendBroadcast(intent);
            }
        }

        for (int i = 0; i < mCubeCount; i++)
        {
            gl.glLoadIdentity();
            float distance = 0;

            distance = ((i + 1) - center) * DISTANCE;
            gl.glTranslatef(mX + distance, 0, -5.5f);
            positionMap.put(i, mX + distance);

            if (positionMap.get(i) == 0)
            {
                selectedPosition = i;
            }

            float angle = -(positionMap.get(i) * ROTATE_ANGLE);

            if (angle < RIGHT_MAX_ROTATE_ANGLE)
            {
                angle = RIGHT_MAX_ROTATE_ANGLE;
            }
            else if (angle > LEFT_MAX_ROTATE_ANGLE)
            {
                angle = LEFT_MAX_ROTATE_ANGLE;
            }

            gl.glRotatef(angle, 0, 1f, 0);

            float scale = CRITERIA * COMPENSATION_VALUE - (Math.abs(positionMap.get(i)) * COMPENSATION_VALUE);

            if (scale > 0)
            {
                if (scale < SCALE_COMPENSATION_VALUE)
                {
                    gl.glScalef(1, 1, 1);
                }
                else
                {
                    gl.glScalef(scale / SCALE_COMPENSATION_VALUE, scale / SCALE_COMPENSATION_VALUE, 1);
                }
            }

            gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID[i]);

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

            cubes.get(i).draw(gl);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        }
    }

    private void loadTexture(GL10 gl, Bitmap bitmap, int index)
    {
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID[index]);

        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

        bitmap.recycle();
    }

    public int getCenterPosition()
    {
        int centerPosition = 0;

        if (mCubeCount % 2 == 0)
            centerPosition = mCubeCount / 2;
        else
            centerPosition = mCubeCount / 2 + 1;

        return centerPosition;
    }

    public boolean isOdds()
    {
        if (mCubeCount % 2 == 0)
            return false;
        return true;
    }

    public void setPosition(float x)
    {
        mX = Math.round(mX * COMPENSATION_VALUE) + x;
        mX = mX / COMPENSATION_VALUE;
        Log.i(">>>>","mX = " + mX);
    }

    public boolean isNeedAutoLock()
    {
        float absMx = Math.abs(mX);

        if (((absMx * COMPENSATION_VALUE) % (CRITERIA * COMPENSATION_VALUE)) == 0)
        {
            return false;
        }

        return true;
    }

    private void moveToExactlyItemPosition()
    {
        float absMx = Math.abs(mX);

        if (CubeSurfaceView.getCubeSurfaceView().DIRECTION == 0)
        {
            if (((absMx * COMPENSATION_VALUE) % (CRITERIA * COMPENSATION_VALUE)) > MAX_REM_VALUE
                || ((absMx * COMPENSATION_VALUE) % (CRITERIA * COMPENSATION_VALUE)) < MIN_REM_VALUE)
            {
                mX = Math.round(mX * COMPENSATION_VALUE) + MOVE_MORE_EXACTLY_VALUE;
                mX = mX / COMPENSATION_VALUE;
            }
            else
            {
                mX = Math.round(mX * COMPENSATION_VALUE) + MOVE_EXACTLY_VALUE;
                mX = mX / COMPENSATION_VALUE;
            }
        }
        else if (CubeSurfaceView.getCubeSurfaceView().DIRECTION == 1)
        {
            if (((absMx * COMPENSATION_VALUE) % (CRITERIA * COMPENSATION_VALUE)) > MAX_REM_VALUE
                || ((absMx * COMPENSATION_VALUE) % (CRITERIA * COMPENSATION_VALUE)) < MIN_REM_VALUE)
            {
                mX = Math.round(mX * COMPENSATION_VALUE) - MOVE_MORE_EXACTLY_VALUE;
                mX = mX / COMPENSATION_VALUE;
            }
            else
            {
                mX = Math.round(mX * COMPENSATION_VALUE) - MOVE_EXACTLY_VALUE;
                mX = mX / COMPENSATION_VALUE;
            }
        }

        CubeSurfaceView.getCubeSurfaceView().requestRenderer();
    }
}
