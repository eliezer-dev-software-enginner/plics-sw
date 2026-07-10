package my_app.services;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public class WinRawPrinter {

    public interface WinSpool extends StdCallLibrary {
        WinSpool INSTANCE = Native.load("winspool.drv", WinSpool.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean OpenPrinterA(String printerName, Pointer[] phPrinter, Pointer pDefault);
        boolean ClosePrinter(Pointer hPrinter);
        boolean StartDocPrinterA(Pointer hPrinter, int level, DOC_INFO_1 docInfo);
        boolean EndDocPrinter(Pointer hPrinter);
        boolean StartPagePrinter(Pointer hPrinter);
        boolean EndPagePrinter(Pointer hPrinter);
        boolean WritePrinter(Pointer hPrinter, byte[] data, int size, int[] written);
    }

    public static class DOC_INFO_1 extends com.sun.jna.Structure {
        public String pDocName;
        public String pOutputFile;
        public String pDataType;

        public DOC_INFO_1(String docName, String dataType) {
            this.pDocName = docName;
            this.pOutputFile = null;
            this.pDataType = dataType; // "RAW" é o que importa
        }

        @Override
        protected java.util.List<String> getFieldOrder() {
            return java.util.List.of("pDocName", "pOutputFile", "pDataType");
        }
    }

    public static boolean imprimirRaw(String nomeImpressora, byte[] bytes) {
        Pointer[] hPrinter = new Pointer[1];
        WinSpool spool = WinSpool.INSTANCE;

        if (!spool.OpenPrinterA(nomeImpressora, hPrinter, null)) return false;
        try {
            DOC_INFO_1 docInfo = new DOC_INFO_1("Comprovante ESC/POS", "RAW");
            if (!spool.StartDocPrinterA(hPrinter[0], 1, docInfo)) return false;
            try {
                spool.StartPagePrinter(hPrinter[0]);
                int[] written = new int[1];
                boolean ok = spool.WritePrinter(hPrinter[0], bytes, bytes.length, written);
                spool.EndPagePrinter(hPrinter[0]);
                return ok;
            } finally {
                spool.EndDocPrinter(hPrinter[0]);
            }
        } finally {
            spool.ClosePrinter(hPrinter[0]);
        }
    }
}