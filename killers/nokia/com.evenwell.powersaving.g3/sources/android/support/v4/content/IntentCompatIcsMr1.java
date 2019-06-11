package android.support.v4.content;

import android.content.Intent;

class IntentCompatIcsMr1 {
    IntentCompatIcsMr1() {
    }

    public static Intent makeMainSelectorActivity(String selectorAction, String selectorCategory) {
        return Intent.makeMainSelectorActivity(selectorAction, selectorCategory);
    }
}
