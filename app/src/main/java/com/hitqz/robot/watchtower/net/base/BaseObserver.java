package com.hitqz.robot.watchtower.net.base;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.sonicers.commonlib.widget.LoadingDialog;

import io.reactivex.observers.DisposableObserver;
import retrofit2.HttpException;

/**
 * 剥离数据的观察者
 */
public abstract class BaseObserver<M> extends DisposableObserver<BaseRespond<M>> {

    public abstract void onSuccess(M model);

    public abstract void onFailure(String msg);

    private LoadingDialog mLoadingDialog;

    public BaseObserver() {

    }

    /**
     * @param dialog null
     */
    public BaseObserver(@Nullable LoadingDialog dialog) {
        mLoadingDialog = dialog;
    }

    @Override
    protected void onStart() {
        showLoadingDialog();
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
        if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            int code = httpException.code();
            String msg = httpException.getMessage();
            if (code == 504) {
                msg = "网络不给力";
            }
            if (code == 502 || code == 404) {
                msg = "服务器异常，请稍后再试";
            }
            onFailure(msg);
        } else {
            onFailure(throwable.getMessage());
        }
        dismissLoadingDialog();
    }

    /**
     * 剥离需要的数据返回给上层
     * 接口请求成功 并不代表真正的成功
     * 需要对业务的code 判断  再分情况返回给上层
     */
    @Override
    public void onNext(BaseRespond<M> mBaseRespond) {
        if ("SUCCESS".endsWith(mBaseRespond.getStatus())) {
            onSuccess(mBaseRespond.getData());
        } else {
            onFailure("服务器Status返回Fail");
        }
    }

    @Override
    public void onComplete() {
        dismissLoadingDialog();
    }

    public void showLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.show();
        }
    }

    public void dismissLoadingDialog() {

        if (mLoadingDialog != null) {
            Activity activity = mLoadingDialog.getContext();
            if (isInvalidContext(activity)) {
                return;
            }
            mLoadingDialog.hide();
        }
    }

    private boolean isInvalidContext(Activity activity) {
        if (activity == null) {
            return false;
        }
        return (activity.isDestroyed() || activity.isFinishing());
    }
}
