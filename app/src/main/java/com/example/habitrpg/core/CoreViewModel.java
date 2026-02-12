package com.example.habitrpg.core;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public abstract class CoreViewModel<S, A, SE> extends ViewModel {
    protected final MutableLiveData<S> state = new MutableLiveData<>();
    protected final SingleLiveEvent<SE> sideEffect = new SingleLiveEvent<>();

    public LiveData<S> getState() { return state; }
    public LiveData<SE> getEffect() { return sideEffect; }

    public abstract void handleAction(A action);
}