package com.aromasoft;

import android.annotation.TargetApi;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class CubeSurfaceView extends GLSurfaceView implements GestureDetector.OnGestureListener
{
    private CubeRenderer mRenderer;
    private GestureDetector mGestureDetector = null;

    float prex;

    private static CubeSurfaceView mCubeSurfaceView;

    private final float COMPENSATION_MOVE_VALUE = 0.1f;
    private final int COMPENSATION_VALUE = 100;

    public final int LEFT_DIRECTION = 0;
    public final int RIGHT_DIRECTION = 1;

    public final float MIN_MOVE_VALUSE = 10.0f;

    public int DIRECTION;

    public CubeSurfaceView(Context context)
    {
        super(context);
        mCubeSurfaceView = this;
        setGestureDetector(context);
    }

    public CubeSurfaceView(Context context, AttributeSet attrs)
    {
        super(context);
        mCubeSurfaceView = this;
        setGestureDetector(context);
    }

    public void setCubeRenderer(Context context, int cubeCount)
    {
        mRenderer = new CubeRenderer(mCubeSurfaceView, context, cubeCount);
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public static CubeSurfaceView getCubeSurfaceView()
    {
        return mCubeSurfaceView;
    }

    private void setGestureDetector(Context context)
    {
        mGestureDetector = new GestureDetector(context, this);
        mGestureDetector.setIsLongpressEnabled(false);
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public boolean onTouchEvent(final MotionEvent event)
    {
        final float w = getWidth() / 2;
        mGestureDetector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            float x = ((event.getX() * 2) / w) - 1.0f;

            x = Math.round(x * COMPENSATION_VALUE);
            x = x / COMPENSATION_VALUE;
            prex = event.getX();

        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE)
        {
            float x = event.getX();
            x = Math.round(x * COMPENSATION_VALUE);
            x = x / COMPENSATION_VALUE;
            if (x - prex > 0 && x - prex > MIN_MOVE_VALUSE)
            {
                DIRECTION = LEFT_DIRECTION;
                if (isPrevOutOfArrange())
                    return true;

                mRenderer.setPosition(x - prex);
                mRenderer.mIsMoving = true;
                prex = x;
            }
            else if (x - prex < 0 && x - prex < -MIN_MOVE_VALUSE)
            {
                DIRECTION = RIGHT_DIRECTION;
                if (isNextOutOfArrange())
                    return true;

                mRenderer.setPosition(x - prex);
                mRenderer.mIsMoving = true;
                prex = x;
            }
        }

        else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP)
        {
            mRenderer.mIsMoving = false;
        }

        requestRender();
        return true;
    }

    public boolean isPrevOutOfArrange()
    {
        if (mRenderer.isOdds())
        {
            if (mRenderer.mX > (mRenderer.DISTANCE * (mRenderer.getCenterPosition() - 1)) - COMPENSATION_MOVE_VALUE)
            {
                mRenderer.mX = mRenderer.DISTANCE * (mRenderer.getCenterPosition() - 1);
                return true;
            }
        }
        else
        {
            if (mRenderer.mX > (mRenderer.DISTANCE * (mRenderer.getCenterPosition()-1)) - COMPENSATION_MOVE_VALUE)
            {
                mRenderer.mX = mRenderer.DISTANCE * (mRenderer.getCenterPosition()-1);
                return true;
            }
        }

        return false;
    }

    public boolean isNextOutOfArrange()
    {
        if (mRenderer.isOdds())
        {
            if (mRenderer.mX < (-mRenderer.DISTANCE * (mRenderer.getCenterPosition() - 1)) + COMPENSATION_MOVE_VALUE)
            {
                mRenderer.mX = -mRenderer.DISTANCE * (mRenderer.getCenterPosition() - 1);
                return true;
            }
        }
        else
        {
            if (mRenderer.mX < (-mRenderer.DISTANCE * (mRenderer.getCenterPosition())) + COMPENSATION_MOVE_VALUE)
            {
                mRenderer.mX = -mRenderer.DISTANCE * (mRenderer.getCenterPosition());
                return true;
            }
        }

        return false;
    }

    public void showPrevious()
    {
        if (isPrevOutOfArrange())
            return;

        else if (mRenderer.isOdds() && mRenderer.mX == mRenderer.DISTANCE * (mRenderer.getCenterPosition() - 1))
        {
            return;
        }
        else if (!mRenderer.isOdds() && mRenderer.mX == mRenderer.DISTANCE * (mRenderer.getCenterPosition()-1))
        {
            return;
        }

        movePrevious();
    }

    private void movePrevious()
    {
        final int beforeX = (int) mRenderer.mX;
        new Thread(new Runnable()
        {
            public void run()
            {
                while (mRenderer.mX < beforeX + mRenderer.DISTANCE)
                {
                    try
                    {
                        mRenderer.mIsMoving = true;
                        float absMx = Math.abs(mRenderer.mX);
                        if (((absMx * COMPENSATION_VALUE) % (mRenderer.CRITERIA * COMPENSATION_VALUE)) > mRenderer.MAX_REM_VALUE
                            || ((absMx * COMPENSATION_VALUE) % (mRenderer.CRITERIA * COMPENSATION_VALUE)) < mRenderer.MIN_REM_VALUE)
                        {
                            mRenderer.setPosition(mRenderer.MOVE_MORE_EXACTLY_VALUE);
                        }

                        else
                        {
                            mRenderer.setPosition(mRenderer.MOVE_EXACTLY_VALUE);
                        }

                        requestRender();
                        Thread.sleep((long) 15);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                mRenderer.mIsMoving = false;
            }
        }).start();
    }

    public void showNext()
    {
        if (isNextOutOfArrange())
        {
            return;
        }
        else if(mRenderer.isOdds() && mRenderer.mX == -mRenderer.DISTANCE * (mRenderer.getCenterPosition() - 1))
        {
                return;
        }
        else if(!mRenderer.isOdds() && mRenderer.mX == -mRenderer.DISTANCE * (mRenderer.getCenterPosition()))
        {
            return;
        }
        
        moveNext();
    }

    private void moveNext()
    {
        final int beforeX = (int) mRenderer.mX;
        new Thread(new Runnable()
        {
            public void run()
            {
                while (mRenderer.mX > beforeX - mRenderer.DISTANCE)
                {
                    try
                    {
                        mRenderer.mIsMoving = true;
                        float absMx = Math.abs(mRenderer.mX);
                        if (((absMx * COMPENSATION_VALUE) % (mRenderer.CRITERIA * COMPENSATION_VALUE)) > mRenderer.MAX_REM_VALUE
                            || ((absMx * COMPENSATION_VALUE) % (mRenderer.CRITERIA * COMPENSATION_VALUE)) < mRenderer.MIN_REM_VALUE)
                        {
                            mRenderer.setPosition(-mRenderer.MOVE_MORE_EXACTLY_VALUE);
                        }

                        else
                        {
                            mRenderer.setPosition(-mRenderer.MOVE_EXACTLY_VALUE);
                        }

                        requestRender();
                        Thread.sleep((long) 15);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                mRenderer.mIsMoving = false;
            }
        }).start();
    }

    public void requestRenderer()
    {
        requestRender();
    }

    public boolean onDown(MotionEvent e)
    {
        return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        return true;
    }

    public void onLongPress(MotionEvent e)
    {
        return;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        return false;
    }

    public void onShowPress(MotionEvent e)
    {
        return;
    }

    public boolean onSingleTapUp(MotionEvent e)
    {
        final float w = getWidth() / 2;
        if (mRenderer.mIsMoving == false)
        {
            float x = ((e.getX() * 2) / w) - 1.0f;
            x = Math.round(x * COMPENSATION_VALUE);
            x = x / COMPENSATION_VALUE;
            if (x > 2)
            {
                showNext();
            }
            else if (x < 0)
            {
                showPrevious();
            }
        }
        return true;
    }
}