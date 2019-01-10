package com.evenwell.powersaving.g3.utils;

import android.util.Xml;
import com.evenwell.powersaving.g3.provider.PowerSavingProvider.SettingsColumns;
import com.evenwell.powersaving.g3.utils.PSConst.COMMON.INTENT.FUNCTION.EXTRA;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;

public class DbgConfig {
    public static final String DefaultDbgConfig_PATH = "system/etc/DefaultDbgConfig_release.xml";
    public static final String TAG = "DbgConfig";
    private static DbgConfig instance;
    public Map<String, String> logConfigMap = new HashMap();
    public Map<String, Program> programs = new HashMap();

    public class Arg {
        public String name = "";
        public Map<String, Param> params = new HashMap();
    }

    static class LoggerConfig {
        static final String TAG = "LoggerConfig";

        static class Entry {
            static final String Clear = "Clear";
            static final String DebugFS = "DebugFS";
            static final String Enable = "Enable";

            Entry() {
            }
        }

        LoggerConfig() {
        }
    }

    public class Param {
        public String name = "";
        public String value = "";
    }

    public class Program {
        public Map<String, Arg> args = new HashMap();
        public String name = "";
    }

    static class ProgramTag {
        static final String DumpSystemInfo = "DumpSystemInfo";
        static final String ModemLinkCfg = "ModemLinkCfg";
        static final String TAG = "program";
        static final String klogd = "klogd";
        static final String logcat_events = "logcat_events";
        static final String logcat_fih = "logcat_fih";
        static final String logcat_main = "logcat_main";
        static final String logcat_radio = "logcat_radio";
        static final String logcat_system = "logcat_system";

        static class Arg {
            static final String Buffer = "Buffer";
            static final String Enable = "Enable";
            static final String Exe = "Exe";
            static final String File = "File";
            static final String Filter = "Filter";
            static final String From = "From";
            static final String Level = "Level";
            static final String MsgFmt = "MsgFmt";
            static final String RotateCnt = "RotateCnt";
            static final String Size = "Size";
            static final String TAG = "arg";
            static final String To = "To";
            static final String WakeLock = "WakeLock";

            static class param {
                static final String Enable = "Enable";
                static final String TAG = "param";
                static final String dash = "dash";
                static final String value = "value";

                param() {
                }
            }

            Arg() {
            }
        }

        ProgramTag() {
        }
    }

    public static DbgConfig getInstance() {
        if (instance == null) {
            instance = new DbgConfig();
        }
        return instance;
    }

    private DbgConfig() {
        read(DefaultDbgConfig_PATH);
    }

    private void read(String path) {
        try {
            FileInputStream stream = new FileInputStream(path);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            Program program = null;
            Arg arg = null;
            while (parser != null) {
                int type = parser.next();
                String tag = parser.getName();
                if (type == 2) {
                    String attributeValue;
                    if ("LoggerConfig".equals(tag)) {
                        attributeValue = parser.getAttributeValue(null, SettingsColumns.NAME);
                        if (EXTRA.POWERSAVER_ENABLE.equals(attributeValue)) {
                            this.logConfigMap.put(EXTRA.POWERSAVER_ENABLE, parser.nextText());
                        }
                        if ("Clear".equals(attributeValue)) {
                            this.logConfigMap.put("Clear", parser.nextText());
                        }
                        if ("DebugFS".equals(attributeValue)) {
                            this.logConfigMap.put("DebugFS", parser.nextText());
                        }
                    }
                    if ("program".equals(tag)) {
                        attributeValue = parser.getAttributeValue(null, SettingsColumns.NAME);
                        program = new Program();
                        program.name = attributeValue;
                    } else if ("arg".equals(tag)) {
                        attributeValue = parser.getAttributeValue(null, SettingsColumns.NAME);
                        arg = new Arg();
                        arg.name = attributeValue;
                    } else if ("param".equals(tag)) {
                        attributeValue = parser.getAttributeValue(null, SettingsColumns.NAME);
                        String value = parser.nextText();
                        Param param = new Param();
                        param.name = attributeValue;
                        param.value = value;
                        arg.params.put(param.name, param);
                    }
                } else if (type == 3) {
                    if ("arg".equals(tag)) {
                        program.args.put(arg.name, arg);
                    } else if ("program".equals(tag)) {
                        this.programs.put(program.name, program);
                    }
                }
                if (type == 1) {
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isLogcatMainOn() {
        try {
            return ((Param) ((Arg) ((Program) this.programs.get("logcat_main")).args.get(EXTRA.POWERSAVER_ENABLE)).params.get(EXTRA.POWERSAVER_ENABLE)).value.equals("1");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
