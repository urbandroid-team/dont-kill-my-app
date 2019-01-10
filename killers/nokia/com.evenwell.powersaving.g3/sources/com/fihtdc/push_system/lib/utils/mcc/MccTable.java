package com.fihtdc.push_system.lib.utils.mcc;

import java.util.ArrayList;
import java.util.Collections;

public final class MccTable {
    static ArrayList<MccEntry> sTable = null;
    final String LOG_TAG = "MccTable";

    class MccEntry implements Comparable<MccEntry> {
        String mIso;
        String mLanguage;
        int mMcc;
        int mSmallestDigitsMnc;

        MccEntry(MccTable this$0, int mnc, String iso, int smallestDigitsMCC) {
            this(mnc, iso, smallestDigitsMCC, null);
        }

        MccEntry(int mnc, String iso, int smallestDigitsMCC, String language) {
            this.mMcc = mnc;
            this.mIso = iso;
            this.mSmallestDigitsMnc = smallestDigitsMCC;
            this.mLanguage = language;
        }

        public int compareTo(MccEntry o) {
            if (this == o) {
                return 0;
            }
            return this.mMcc - o.mMcc;
        }
    }

    MccTable() {
        init();
    }

    private MccEntry entryForMcc(int mcc) {
        int index = Collections.binarySearch(sTable, new MccEntry(this, mcc, null, 0));
        if (index < 0) {
            return null;
        }
        return (MccEntry) sTable.get(index);
    }

    public String countryCodeForMcc(int mcc) {
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            return "";
        }
        return entry.mIso;
    }

    public String defaultLanguageForMcc(int mcc) {
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            return null;
        }
        return entry.mLanguage;
    }

    public int smallestDigitsMccForMnc(int mcc) {
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            return 2;
        }
        return entry.mSmallestDigitsMnc;
    }

    private void init() {
        if (sTable == null) {
            sTable = new ArrayList(4);
            sTable.add(new MccEntry(460, "cn", 2, "zh"));
            sTable.add(new MccEntry(461, "cn", 2, "zh"));
            sTable.add(new MccEntry(this, 466, "tw", 2));
            Collections.sort(sTable);
        }
    }
}
