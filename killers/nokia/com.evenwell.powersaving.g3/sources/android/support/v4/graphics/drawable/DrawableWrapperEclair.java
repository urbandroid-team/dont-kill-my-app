package android.support.v4.graphics.drawable;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.support.annotation.Nullable;

class DrawableWrapperEclair extends DrawableWrapperDonut {

    private static class DrawableWrapperStateEclair extends DrawableWrapperState {
        DrawableWrapperStateEclair(@Nullable DrawableWrapperState orig, @Nullable Resources res) {
            super(orig, res);
        }

        public Drawable newDrawable(@Nullable Resources res) {
            return new DrawableWrapperEclair(this, res);
        }
    }

    DrawableWrapperEclair(Drawable drawable) {
        super(drawable);
    }

    DrawableWrapperEclair(DrawableWrapperState state, Resources resources) {
        super(state, resources);
    }

    DrawableWrapperState mutateConstantState() {
        return new DrawableWrapperStateEclair(this.mState, null);
    }

    protected Drawable newDrawableFromState(ConstantState state, Resources res) {
        return state.newDrawable(res);
    }
}
