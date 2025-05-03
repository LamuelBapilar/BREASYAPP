package com.example.breasyapp2;

public class observeisRecording {

    private boolean value;
    private OnChangeListener listener;

    public interface OnChangeListener {
        void onChange(boolean newValue);
    }

    public void setOnChangeListener(OnChangeListener listener) {
        this.listener = listener;
    }

    public void set(boolean newValue) {
        if (this.value != newValue) {
            this.value = newValue;
            if (listener != null) {
                listener.onChange(newValue);
            }
        }
    }

    public boolean get() {
        return value;
    }
}
