package android.support.v4.widget;

import android.widget.ListView;

class ListViewCompatKitKat {
    ListViewCompatKitKat() {
    }

    static void scrollListBy(ListView listView, int y) {
        listView.scrollListBy(y);
    }
}
