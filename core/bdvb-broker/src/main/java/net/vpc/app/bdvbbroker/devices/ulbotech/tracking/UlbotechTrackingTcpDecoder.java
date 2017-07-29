package net.vpc.app.bdvbbroker.devices.ulbotech.tracking;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.vpc.app.bdvbbroker.BdvbTcpConnection;
import net.vpc.app.bdvbbroker.BdvbTcpDecoder;
import net.vpc.app.bdvbbroker.DefaultBdvbPacket;
import net.vpc.app.bdvbbroker.RichInputStream;
import net.vpc.app.bdvbbroker.util.ByteArrayList;
import net.vpc.app.bdvbbroker.util.Utils;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by vpc on 10/30/16.
 */
public class UlbotechTrackingTcpDecoder implements BdvbTcpDecoder {
    public static final String RESERVED = "Reserved";
    private static final String[] infoStatus = {
            "Power cut",
            "Moving",
            "Over speed",
            "Jamming",
            "Geo-fence alarming",
            "Immobolizer",
            "ACC",
            "Input high level",
            "Input mid level",
            "Engine",
            "Panic",
            "OBD alarm",
            "Course rapid change",
            "Speed rapid change",
            "Roaming",
            "Inter roaming"};
    private static final String[] infoAlarm = {"Power cut",
            "Moved",
            "Over speed",
            "Jamming",
            "Geo-fence",
            "Towing",
            RESERVED,
            "Input low",
            "Input high",
            RESERVED,
            "Panic",
            "OBD",
            RESERVED,
            RESERVED,
            "Accident",
            "Battery low"};
    private static final int MinPacketLen = 22;
    private static final int ProtocolVersion = 1;
    private static final byte txtStartChar = (byte) '*';
    private static final byte txtEndChar = (byte) '#';
    private static final byte binFlagChar = (byte) 0xF8;
    private static final byte binEscapeChar = (byte) 0xF7;
    private static final byte AckFlag = (byte) 0xFE;
    private static final SimpleDateFormat hHmmssddMMyy = new SimpleDateFormat(
            "HHmmssddMMyy"
    );
    private static final Date DEFAULT_DATE;
    private static String[] infoBehavior = {
            "Rapid acceleration",
            "Rough braking",
            "Harsh course",
            "No warmup",
            "Long idle",
            "Fatigue driving",
            "Rough terrain",
            "High RPM"
    };
    private static String[] infoAdcNames = {"Car Battery",
            "Device Temperature",
            "Inner Battery",
            "Input voltage"};
    private static String[] infoAdcUnits = {"(V)", "(Celsius)", "(V)", "(V)"};

    static {
        try {
            DEFAULT_DATE = hHmmssddMMyy.parse("000000010100");
        } catch (ParseException e1) {
            throw new RuntimeException(e1);
        }

    }

    String[] eventInfo0 = {"None",
            "Interval upload",
            "Angle change upload",
            "Distance upload",

            "Request upload"};

//    FileStream tempFile = null;
//    StreamWriter tempWrite = null;
    String[] eventInfo1 = {"Rfid reader",
            "iBeacon"};

    private static void arrayCopy(byte[] src, int srcOffset, byte[] dest, int destOffset, int count) {
        System.arraycopy(src, srcOffset, dest, destOffset, count);
    }


//    private void btn_decode_Click(object sender, EventArgs e)
//    {
//        FileStream fs;
//        progressBar1.Value = 0;
//        if (openFileDialog.ShowDialog() == DialogResult.Cancel)
//        {
//            return;
//        }
//        try
//        {
//            fs = new FileStream(openFileDialog.FileName, FileMode.Open, FileAccess.Read);
//        }
//        catch (System.Exception ex)
//        {
//            MessageBox.Show(ex.Message);
//            return;
//        }
//        byte[] readBuffer = new byte[(int)fs.Length];
//        int dataLength = fs.Read(readBuffer, 0, (int)fs.Length);
//        fs.Close();
//
//        String path = System.AppDomain.CurrentDomain.BaseDirectory;
//        path += "ulbodecode.tmp";
//        try
//        {
//            tempFile = new FileStream(path, FileMode.OpenOrCreate, FileAccess.ReadWrite);
//        }
//        catch (System.Exception ex)
//        {
//            MessageBox.Show(ex.Message);
//            return;
//        }
//        tempFile.SetLength(0);
//        tempWrite = new StreamWriter(tempFile);
//
//
//        DataDecode(readBuffer);
//
//        tempWrite.Close();
//        tempFile.Close();
//        StreamReader tempReader;
//        tempReader = new StreamReader(path);
//        String str = tempReader.ReadToEnd();
//        rtb_decode_info.AppendText(str);
//        tempReader.Close();
//        tempReader.Dispose();
//        System.IO.FileInfo fi = new System.IO.FileInfo(@path);
//        try
//        {
//            fi.Delete();
//        }
//        catch
//        {
//        }
//        //MessageBox.Show("Decode finished");
//    }
//
//    private void btn_decode_txt_Click(object sender, EventArgs e)
//    {
//        progressBar1.Value = 0;
//        if (txt_text.Text == "")
//        {
//            MessageBox.Show("Pls input text data!");
//            return;
//        }
//        byte[] textData = System.Text.Encoding.Default.GetBytes(txt_text.Text);
//
//        String path = System.AppDomain.CurrentDomain.BaseDirectory;
//        path += "ulbodecode.tmp";
//        try
//        {
//            tempFile = new FileStream(path, FileMode.OpenOrCreate, FileAccess.ReadWrite);
//        }
//        catch (System.Exception ex)
//        {
//            MessageBox.Show(ex.Message);
//            return;
//        }
//        tempFile.SetLength(0);
//        tempWrite = new StreamWriter(tempFile);
//
//        byte[] ack = DataBinAcknowledgement(textData);
//        String hexAck = "";
//        for (int i = 0; i < ack.Length; i++)
//        {
//            hexAck += StringFormat("{0:X2} ", ack[i]);
//        }
//        txt_ack_txt_binframe.Text = hexAck;
//
//        txt_ack_txt_txtframe.Text = DataTxtAcknowledgement(textData);
//
//        DataDecode(textData);
//
//        tempWrite.Close();
//        tempFile.Close();
//        StreamReader tempReader;
//        tempReader = new StreamReader(path);
//        String str = tempReader.ReadToEnd();
//        rtb_decode_info.AppendText(str);
//        tempReader.Close();
//        tempReader.Dispose();
//        System.IO.FileInfo fi = new System.IO.FileInfo(@path);
//        try
//        {
//            fi.Delete();
//        }
//        catch
//        {
//        }
//        //MessageBox.Show("Decode finished");
//    }
//
//    private void btn_decode_bin_Click(object sender, EventArgs e)
//    {
//        progressBar1.Value = 0;
//        if (txt_binary.Text == "")
//        {
//            MessageBox.Show("Pls input binary data!");
//            return;
//        }
//        String strHex = txt_binary.Text;
//        strHex = strHex.Replace(" ", "");
//        strHex = strHex.Replace("\r", "");
//        strHex = strHex.Replace("\n", "");
//        strHex = strHex.Replace("\t", "");
//        if (strHex.Length % 2 != 0)
//        {
//            MessageBox.Show("Data format error!");
//            return;
//        }
//        strHex = strHex.ToUpper();
//        for (int i = 0; i < strHex.Length; i++)
//        {
//            if (strHex[i] < '0' || strHex[i] > 'F' || (strHex[i] > '9' && strHex[i] < 'A'))
//            {
//                MessageBox.Show("Data format error!");
//                return;
//            }
//        }
//        byte[] binData = new byte[strHex.Length / 2];
//        for (int i = 0; i < strHex.Length / 2; i++)
//        {
//            binData[i] = Convert.ToByte(strHex.Substring(2 * i, 2), 16);
//        }
//
//        String path = System.AppDomain.CurrentDomain.BaseDirectory;
//        path += "ulbodecode.tmp";
//        try
//        {
//            tempFile = new FileStream(path, FileMode.OpenOrCreate, FileAccess.ReadWrite);
//        }
//        catch (System.Exception ex)
//        {
//            MessageBox.Show(ex.Message);
//            return;
//        }
//        tempFile.SetLength(0);
//        tempWrite = new StreamWriter(tempFile);
//
//        byte[] ack = DataBinAcknowledgement(binData);
//        String hexAck = "";
//        for (int i = 0; i < ack.Length; i++)
//        {
//            hexAck += StringFormat("{0:X2} ", ack[i]);
//        }
//        txt_ack_bin_binframe.Text = hexAck;
//
//        txt_ack_bin_txtframe.Text = DataTxtAcknowledgement(binData);
//
//        DataDecode(binData);
//
//        tempWrite.Close();
//        tempFile.Close();
//        StreamReader tempReader;
//        tempReader = new StreamReader(path);
//        String str = tempReader.ReadToEnd();
//        rtb_decode_info.AppendText(str);
//        tempReader.Close();
//        tempReader.Dispose();
//        System.IO.FileInfo fi = new System.IO.FileInfo(@path);
//        try
//        {
//            fi.Delete();
//        }
//        catch
//        {
//        }
//        //MessageBox.Show("Decode finished");
//    }
//
//    private void btn_save_Click(object sender, EventArgs e)
//    {
//        String fileName = "";
//        if (saveFileDialog.ShowDialog() == DialogResult.Cancel)
//        {
//            return;
//        }
//
//        fileName = saveFileDialog.FileName;
//        try
//        {
//            StreamWriter streamWriter = new StreamWriter(fileName);
//            streamWriter.Write(rtb_decode_info.Text);
//            streamWriter.Close();
//        }
//        catch (System.Exception ex)
//        {
//            MessageBox.Show(ex.Message);
//        }
//    }

    private static int arrayIndexOf(byte[] items, int item, int from) {
        for (int i = from; i < items.length; i++) {
            if (items[i] == item) {
                return i;
            }
        }
        return -1;
    }

    private static String StringFormat(String msg, Object... args) {
        throw new RuntimeException("Unsupported StringFormat");
    }

    public static void main(String[] args) {
        System.out.println(parseByte("8F", 16));
    }

    public static byte parseByte(String s, int radix)
            throws NumberFormatException {
        int x = Integer.parseInt(s, radix);
        if (x > 127) {
            return (byte) (x & 0xFF);
        }
        return (byte) x;
    }

    public void decode(BdvbTcpConnection connection) throws IOException {
        DataDecode0(connection.in, new FrameListener() {
            @Override
            public void onReadFrame(JsonObject raw) {
                JsonObject g = (JsonObject) raw;
                DefaultBdvbPacket packet = new DefaultBdvbPacket();
                packet.setRaw(raw);
                packet.setDeviceDriver(connection.driverId);
                packet.setDeviceUUID(g.getAsJsonPrimitive("deviceId").getAsString());
                packet.setServerTime(new Date());
                packet.setDeviceTime(Utils.parseDateOrNull(raw.get("timestamp").getAsString()));
                packet.setDeviceAddress(connection.socket.getInetAddress().toString());
                packet.setDeviceFullAddress(connection.socket.getRemoteSocketAddress().toString());
                packet.setDeviceType("UlbotechT361");

//                packet.setValue(uniform);
//                ValueNode gps = ((ValueGroup) raw).getNode("GPS");
//                if(gps!=null){
//                    uniform.add(gps.copy());
//                }
//
//                ValueNode adc = ((ValueGroup) raw).getNode("ADC");
//                if(adc!=null){
//                    ValueGroup adc2 = new ValueGroup("ADC");
//                    uniform.add(adc2);
//                }
                if (connection.broker != null) {
                    connection.broker.publish(packet);
                }
            }
        });
    }

    public List<JsonObject> DataDecode(byte[] buffer) throws IOException {
        List<JsonObject> list = new ArrayList<>();
        DataDecode0(buffer, new FrameListener() {
            @Override
            public void onReadFrame(JsonObject f) {
                list.add(f);
            }
        });
        return list;
    }

    public void DataDecode0(RichInputStream in, FrameListener listener) throws IOException {
        ByteArrayList a = new ByteArrayList();
        while (true) {
            int d = in.read();
            if (d == -1) {
                throw new java.io.EOFException();
            } else if (d == txtStartChar) {
                a.add((byte) d);
                while (true) {
                    d = in.read();
                    a.add((byte) d);
                    if (d == txtEndChar) {
                        break;
                    }
                }
                DataDecode0(a.toArray(), listener);
                break;
            } else if (d == binFlagChar) {
                a.add((byte) d);
                while (true) {
                    d = in.read();
                    a.add((byte) d);
                    if (d == binFlagChar) {
                        break;
                    }
                }
                DataDecode0(a.toArray(), listener);
                break;
            }
        }
    }

    public void DataDecode0(byte[] buffer, FrameListener listener) throws IOException {
        if (buffer.length == 0) {
//            OutputText("Empty data!");
            return;
        }
//        rtb_decode_info.Clear();
//        OutputText("Starting decode...\r\n");
        int Offset = 0;
//            progressBar1.Maximum = buffer.length;

        while (Offset + MinPacketLen < buffer.length) {
//            progressBar1.Value = Offset;
            if (buffer[Offset] == txtStartChar)//Text frame start flag
            {

                int endPos = arrayIndexOf(buffer, (txtEndChar), Offset + 1);//Get Text frame end flag position
                if (endPos == -1 || (endPos - Offset) < 20 || !TextFrameFormatCheck(buffer, Offset, endPos))//Check Text frame format
                {
                    Offset++;
                    continue;
                }

                String strFrame = new String(buffer, Offset, endPos - Offset + 1, "UTF-8");
                JsonObject f = TextFrameDecode(strFrame);
                if (f != null) {
                    listener.onReadFrame(f);
                }
                Offset = endPos + 1;
            } else if (buffer[Offset] == binFlagChar)//Binary packet flag
            {
                int endPos = arrayIndexOf(buffer, (binFlagChar), Offset + 1);//Get binary frame end flag position
                if (endPos == -1 || (endPos - Offset) < 12) {
                    Offset++;
                    continue;
                }

                String strHexFrame = "";
                for (int i = Offset; i < endPos + 1; i++) {
                    strHexFrame += StringFormat("{0:X2} ", buffer[i]);
                }
                OutputText(StringFormat("BIN frame: {0}\r\n", strHexFrame));

                byte[] binFrame = new byte[endPos - Offset - 1];
                arrayCopy(buffer, Offset + 1, binFrame, 0, binFrame.length);
                int datalen = BinFrameFormatCheck(binFrame);
                if (datalen <= 0) {
                    OutputText("    CRC check error\r\n");
                    Offset++;
                    continue;
                }
                JsonObject f = BinFrameDecode(binFrame, datalen);
                if (f != null) {
                    listener.onReadFrame(f);
                }
                Offset = endPos + 1;
            } else {
                Offset++;
            }
        }
//        progressBar1.Value = buffer.length;
//        OutputText("Decode finished!\r\n");
    }

    boolean TextFrameFormatCheck(byte[] buffer, int first, int end) {
        String frameFirst = ("*TS" + Utils.formatDecimal(ProtocolVersion, 2));
        for (int i = 0; i < frameFirst.length(); i++) {
            if (buffer[first + i] != frameFirst.charAt(i))
                return false;
        }
        if (buffer[end] != txtEndChar)
            return false;
        for (int i = first + frameFirst.length(); i < end; i++) {
            if (buffer[i] < 0x20 || buffer[i] > 0x7F || buffer[i] == txtStartChar)
                return false;
        }
        return true;
    }

    int BinFrameFormatCheck(byte[] binFrame) {
        boolean bEscape = false;
        int len = 0;
        for (int i = 0; i < binFrame.length; i++) {
            if (bEscape) {
                bEscape = false;
                binFrame[len++] = (byte) (binFrame[i] ^ binEscapeChar);
            } else {
                if (binFrame[i] == binEscapeChar) {
                    bEscape = true;
                    continue;
                } else {
                    binFrame[len++] = binFrame[i];
                }
            }
        }
        if (GetCrc16Value(binFrame, len) != 0)
            return 0;
        return len - 2;
    }

    Date GetDateTimeFromString(String str) {
        try {
            return hHmmssddMMyy.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            return DEFAULT_DATE;
        }
//        DateTime dt;
//        try
//        {
//            dt = new DateTime(
//                    System.Convert.ToInt16(str.substring(10, 2)) + 2000,
//                    System.Convert.ToInt16(str.substring(8, 2)),
//                    System.Convert.ToInt16(str.substring(6, 2)),
//                    System.Convert.ToInt16(str.substring(0, 2)),
//                    System.Convert.ToInt16(str.substring(2, 2)),
//                    System.Convert.ToInt16(str.substring(4, 2)));
//        }
//        catch
//        {
//            dt = new DateTime(2000, 1, 1, 0, 0, 0);
//        }
//        return dt;
    }

    JsonObject TextFrameDecode(String str) {
        String TabChars = "    ";
        String[] infoKeyWords = {"GPS", "LBS", "STT", "MGR", "ADC", "GFS", "OBD", "OAL", "FUL", "HDB", "CAN", "HVD", "VIN", "RFI", "EVT", "BCN", "EGT", "TRP"};
        //OutputText(StringFormat("TXT frame: {0}\r\n", str));
        str = str.substring(0, str.length() - 1);
        String[] info = str.split(",");
        if (info.length < 4 || !Objects.equals(info[0], ("*TS" + Utils.formatDecimal(ProtocolVersion, 2)))) {
            //OutputText(StringFormat("{0}Frame error!\r\n", TabChars));
            //OutputText("\r\n");
            return null;
        }
        JsonObject frame = new JsonObject();
        frame.addProperty("deviceId", info[1]);

        if (info[2].length() != 12 || info[2].equals("000000000000")) {
//            OutputText(StringFormat("{0}Time stamp: {1}\r\n", TabChars, "Unknown"));
        } else {
            frame.addProperty("timestamp", Utils.UNIVERSAL_DATETIME_FORMAT.format(GetDateTimeFromString(info[2])));
//            OutputText(StringFormat("{0}Time stamp: {1}\r\n", TabChars, GetDateTimeFromString(info[2])));
        }
        boolean hdbVisited = false;
        for (int i = 3; i < info.length; i++) {
            int keyIndex;
            int keyId = 0;
            String[] strFields = info[i].split(":");
            if (strFields.length != 2 || strFields[1].equals(""))
                continue;
            for (keyIndex = 0; keyIndex < infoKeyWords.length; keyIndex++) {
                if (strFields[0].startsWith(infoKeyWords[keyIndex])) {
                    if (strFields[0].length() > 3)
                        keyId = Integer.parseInt(strFields[0].substring(3));
                    break;
                }
            }

            switch (keyIndex) {
                case 0://GPS--GPS data
                    frame.add("GPS", GpsDecodeFromString(strFields[1]));
                    break;
                case 1://LBS--LBS data
                    frame.add("LBS", LbsDecodeFromString(strFields[1]));
                    break;
                case 2://STT--Device status data
                    frame.add("STT", SttDecodeFromString(strFields[1]));
                    break;
                case 3://MGR--Mileage
                    frame.add("MGR", MgrDecodeFromString(strFields[1]));
                    break;
                case 4://ADC--Analog data
                    frame.add("ADC", AdcDecodeFromString(strFields[1]));
                    break;
                case 5://GFS--Geo-fence status
                    frame.add("GFS", GfsDecodeFromString(strFields[1]));
                    break;
                case 6://OBD--OBD data
                    frame.add("OBD", ObdDecodeFromString(strFields[1]));
                    break;
                case 7://OAL OBD alarm data
                    frame.add("OAL", OalDecodeFromString(strFields[1]));
                    break;
                case 8://FUL--Fuel used data
                    frame.add("FUL", FulDecodeFromString(strFields[1], keyId));
                    break;
                case 9://HDB--Driver behavior
                    hdbVisited = true;
                    frame.add("HDB", HdbDecodeFromString(strFields[1]));
                    break;
                case 10://CAN--J1939 data
                    frame.add("CAN", CanDecodeFromString(strFields[1]));
                    break;
                case 11://HVD--J1708 data
                    frame.add("HDV", HvdDecodeFromString(strFields[1]));
                    break;
                case 12://VIN--VIN data
                    frame.add("VIN", VinDecodeFromString(strFields[1]));
                    break;
                case 13://RFI--RFID data
                    frame.add("RFI", RfiDecodeFromString(strFields[1]));
                    break;
                case 14://EVT--Event code data
                    frame.add("EVT", EvtDecodeFromString(strFields[1]));
                    break;
                case 15://BCN--iBeacon info data
                    frame.add("BCN", BcnDecodeFromString(strFields[1]));
                    break;
                case 16://EGT--Engine seconds
                    frame.add("EGT", EgtDecodeFromString(strFields[1]));
                    break;
                case 17://TRP--Trip report
                    frame.add("TRP", TrpDecodeFromString(strFields[1]));
                    break;
                default:
                    frame.add("CMD", CmdDecodeFromString(strFields[0], strFields[1]));
                    break;
            }
        }
        if (!hdbVisited) {
            frame.add("HDB", HdbDecodeFromString(null));
        }
        return frame;
    }

    JsonObject BinFrameDecode(byte[] dat, int len) {
        if (len < 10)
            return null;
        int pos = 0;
        if (dat[pos] != ProtocolVersion) {
//            OutputText(StringFormat("    Can not support protocol version: {0:X2}", dat[pos]));
            return null;
        }
        pos++;
        if (dat[pos] != 0x01) {
//            OutputText(StringFormat("    Can not support frame NO: {0:X2}", dat[pos]));
            return null;
        }
        JsonObject frame = new JsonObject();
        pos++;
        String DeviceID = "";
        for (int i = 0; i < 8; i++) {
            byte b = dat[pos++];
            if (i == 0) {
                DeviceID += Integer.toHexString(b);
            } else {
                DeviceID += Utils.formatHex(b, 2);
            }
        }
        String TabChars = "    ";
        OutputText(StringFormat("{0}Device ID: {1}\r\n", TabChars, DeviceID));

        int timeSeconds = ReadUint32(dat, pos);
        pos += 4;

        boolean Fix3D = (timeSeconds & 0x80000000) != 0;
        timeSeconds = timeSeconds & 0x7FFFFFFF;
        if (timeSeconds == 0) {
            OutputText(StringFormat("{0}Time stamp: {1}\r\n", TabChars, "Unknown"));
        } else {
            Date dt = Utils.datePlusSeconds(DEFAULT_DATE, timeSeconds);
            OutputText(StringFormat("{0}Time stamp: {1}\r\n", TabChars, dt));
        }
        while (pos < len - 2) {
            byte infoId = dat[pos++];
            int infoLen;
            switch (infoId) {
                case 1://GPS
                {
                    infoLen = dat[pos++];
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    GpsDecodeFromBinary(infoData, Fix3D);
                    break;
                }
                case 2://LBS
                {
                    infoLen = dat[pos++];
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    LbsDecodeFromBinary(infoData);
                    break;
                }
                case 3://STT
                {
                    infoLen = dat[pos++];
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    SttDecodeFromBinary(infoData);
                    break;
                }
                case 4://MGR
                {
                    infoLen = dat[pos++];
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    MgrDecodeFromBinary(infoData);
                    break;
                }
                case 5://ADC
                {
                    infoLen = dat[pos++];
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    AdcDecodeFromBinary(infoData);
                    break;
                }
                case 6://GFS
                {
                    infoLen = dat[pos++];
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    GfsDecodeFromBinary(infoData);
                    break;
                }
                case 7://OBD
                {
                    infoLen = dat[pos++];
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    ObdDecodeFromBinary(infoData);
                    break;
                }
                case 8://FUL
                {
                    infoLen = dat[pos++];
                    int algorithm = (infoLen >> 4) & 0x0f;
                    infoLen &= 0x0F;
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    FulDecodeFromBinary(infoData, algorithm);
                    break;
                }
                case 9://OAL
                {
                    infoLen = dat[pos++];
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    OalDecodeFromBinary(infoData);
                    break;
                }
                case 0x0A://HDB
                {
                    infoLen = dat[pos++];
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    HdbDecodeFromBinary(infoData);
                    break;
                }
                case 0x0B://CAN--J1939 data
                {
                    infoLen = dat[pos++];
                    infoLen = infoLen * 256 + dat[pos++];
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    CanDecodeFromBinary(infoData);
                    break;
                }
                case 0x0C://HVD--J1708 data
                {
                    infoLen = dat[pos++];
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    HvdDecodeFromBinary(infoData);
                    break;
                }
                case 0x0D://VIN
                {
                    infoLen = dat[pos++];
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    VinDecodeFromBinary(infoData);
                    break;
                }
                case 0x0E://RFI
                {
                    infoLen = dat[pos++];
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    RfiDecodeFromBinary(infoData);
                    break;
                }
                case 0x0F://EGT
                {
                    infoLen = dat[pos++];
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    EgtDecodeFromBinary(infoData);
                    break;
                }
                case 0x10://EVT
                {
                    infoLen = dat[pos++];
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    EvtDecodeFromBinary(infoData);
                    break;
                }
                case 0x20://TRP--Trip report
                {
                    infoLen = dat[pos++];
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    TrpDecodeFromBinary(infoData);
                    break;
                }
                case 0x3F://BCN--iBeacon info data
                {
                    infoLen = dat[pos++];
                    infoLen = infoLen * 256 + dat[pos++];
                    byte[] infoData = new byte[infoLen];
                    arrayCopy(dat, pos, infoData, 0, infoLen);
                    BcnDecodeFromBinary(infoData);
                    break;
                }
                default:
                    infoLen = dat[pos++];
                    break;
            }
            pos += infoLen;
        }
        return frame;
    }

    JsonElement GpsDecodeFromString(String str) {
        String TabChars = "    ";
        String[] gps = str.split(";");
        if (gps.length != 6)
            return null;
        //OutputText(TabChars + "GPS:" + "\r\n");
        JsonObject gpsVal = new JsonObject();
        gpsVal.addProperty("dimension", (gps[0].equals("3") ? 3 : (gps[0].equals("2") ? 2 : 0)));
        gpsVal.addProperty("latitude", (gps[1].substring(0, 1).equals("S") ? "-" : "") + gps[1].substring(1));
        gpsVal.addProperty("longitude", (gps[2].substring(0, 1).equals("W") ? "-" : "") + gps[2].substring(1));
        gpsVal.addProperty("speed", gps[3]);
        gpsVal.addProperty("course", gps[4]);
        gpsVal.addProperty("hdop", gps[5]);
        return gpsVal;
    }

    void GpsDecodeFromBinary(byte[] info, boolean b3d) {
        if (info.length != 14)
            return;

        String TabChars = "    ";
        OutputText(TabChars + "GPS:" + "\r\n");
        TabChars = TabChars + TabChars;
        String gpsfixed;
        if (ReadInt32(info, 0) == 0 && ReadInt32(info, 4) == 0)//Latitude and Longitude all zero
            gpsfixed = "NoFix";
        else
            gpsfixed = b3d ? "3D" : "2D";

        OutputText(StringFormat("{0}Status: {1}\r\n", TabChars, gpsfixed));
        OutputText(StringFormat("{0}Latitude: {1:0.000000}\r\n", TabChars, (double) ReadInt32(info, 0) / 1000000));
        OutputText(StringFormat("{0}Longitude: {1:0.000000}\r\n", TabChars, (double) ReadInt32(info, 4) / 1000000));
        OutputText(StringFormat("{0}Speed: {1}\r\n", TabChars, ReadUint16(info, 8)));
        OutputText(StringFormat("{0}Course: {1}\r\n", TabChars, ReadUint16(info, 10)));
        OutputText(StringFormat("{0}HDOP: {1:0.00}\r\n", TabChars, (double) ReadUint16(info, 12) / 100));
    }

    JsonObject LbsDecodeFromString(String str) {
        String[] lbs = str.split(";");
        if (lbs.length < 5)
            return null;
        JsonObject d = new JsonObject();

        int stations = (lbs.length - 2) / 3;
        d.addProperty("MCC", lbs[0]);
        d.addProperty("MNC", lbs[1]);
        d.addProperty("Stations", stations);
        for (int i = 0; i < stations && i < 7; i++) {
            d.addProperty("Cell[" + i + "] ", lbs[2 + i * 3]);
            d.addProperty("LAC[" + i + "] ", lbs[2 + i * 3]);//,null,"hex"
            d.addProperty("CID[" + i + "] ", lbs[2 + i * 3 + 1]);//,null,"hex"
            d.addProperty("dbm[" + i + "] ", "-" + lbs[2 + i * 3 + 2]);//,null,"dbm"
        }
        return d;
    }

    JsonElement LbsDecodeFromBinary(byte[] info) {
        String TabChars = "    ";
        if (info.length < 9)
            return null;

        OutputText(TabChars + "LBS:" + "\r\n");
        TabChars = TabChars + TabChars;

        if (info.length == 0x0B) {
            OutputText(StringFormat("{0}MCC: {1}(dec), MNC: {2}(dec)\r\n", TabChars, ReadUint16(info, 0), ReadUint16(info, 2)));
            OutputText(StringFormat("{0}Cell: LAC: {1:X4}(hex), CID: {2:X6}(hex), dbm: -{3}\r\n",
                    TabChars,
                    ReadUint16(info, 4),
                    ReadUint32(info, 6),
                    ReadUnsignedByte(info, 10)));
        } else {
            if ((info.length - 4) % 5 != 0)
                return null;
            if ((info.length - 4) / 5 > 7)
                return null;

            OutputText(StringFormat("{0}MCC: {1}(dec), MNC: {2}(dec)\r\n", TabChars, ReadUint16(info, 0), ReadUint16(info, 2)));
            for (int i = 0; i < (info.length - 4) / 5; i++) {
                OutputText(StringFormat("{0}Cell{1}: LAC: {2:X4}(hex), CID: {3:X4}(hex), dbm: -{4}\r\n",
                        TabChars,
                        i,
                        ReadUint16(info, 4 + i * 5),
                        ReadUint16(info, 4 + i * 5 + 2),
                        ReadUnsignedByte(info, 4 + i * 5 + 4)));
            }
        }

        return null;
    }

    JsonObject SttDecodeFromString(String str) {
        String[] stt = str.split(";");
        if (stt.length != 2) {
            return null;
        }
        JsonObject d = new JsonObject();
        int iStatus = Integer.parseInt(stt[0], 16);
        int iAlarm = Integer.parseInt(stt[1], 16);

        d.addProperty("Status", iStatus);
        d.addProperty("Alarm", iAlarm);
        for (int i = 0; i < 16; i++) {
            int value = ((iStatus & ((int) (0x0001 << i))) != 0) ? 1 : 0;
            if (!RESERVED.equals(infoStatus[i]) || value != 0) {
                d.addProperty("Status " + infoStatus[i], value);
            }
        }
        for (int i = 0; i < 16; i++) {
            int value = (iAlarm & ((int) (0x0001 << i))) != 0 ? 1 : 0;
            if (!RESERVED.equals(infoAlarm[i]) || value != 0) {
                d.addProperty("Alarm " + infoAlarm[i], value);
            }
        }

        return d;
    }

    JsonElement SttDecodeFromBinary(byte[] info) {
        String TabChars = "    ";
        if (info.length != 4)
            return null;
        OutputText(TabChars + "STT:" + "\r\n");
        TabChars = TabChars + TabChars;
        int iStatus = ReadUint16(info, 0);
        int iAlarm = ReadUint16(info, 2);
        String[] infoStatus = {"Power cut",
                "Moving",
                "Over speed",
                "Jamming",
                "Geo-fence alarming",
                "Immobolizer",
                "ACC",
                "Input high level",
                "Input mid level",
                "Engine",
                "Panic",
                "OBD alarm",
                "Course rapid change",
                "Speed rapid change",
                "Roaming",
                "Inter roaming"};
        String[] infoAlarm = {"Power cut",
                "Moved",
                "Over speed",
                "Jamming",
                "Geo-fence",
                "Towing",
                RESERVED,
                "Input low",
                "Input high",
                RESERVED,
                "Panic",
                "OBD",
                RESERVED,
                RESERVED,
                "Accident",
                "Battery low"};
        OutputText(TabChars + "Status:" + "\r\n");
        for (int i = 0; i < 16; i++) {
            int bitMask = (int) (0x0001 << i);
            String strStatus = StringFormat("            [Bit{0:D2}]:{1}--{2}\r\n",
                    i,
                    (iStatus & bitMask) != 0 ? 1 : 0,
                    infoStatus[i]);
            OutputText(strStatus);
        }
        OutputText(TabChars + "Alarm:" + "\r\n");
        for (int i = 0; i < 16; i++) {
            int bitMask = (int) (0x0001 << i);
            String strAlarm = StringFormat("            [Bit{0:D2}]:{1}--{2}\r\n",
                    i,
                    (iAlarm & bitMask) != 0 ? 1 : 0,
                    infoAlarm[i]);
            OutputText(strAlarm);
        }
        return null;
    }

    JsonElement MgrDecodeFromString(String str) {
        return new JsonPrimitive(Integer.parseInt(str));//, "Mileage", "meters"
    }

    JsonElement MgrDecodeFromBinary(byte[] info) {
        if (info.length != 4)
            return null;
        return new JsonPrimitive(ReadUint32(info, 0));//, "Mileage", "meters");
    }

    JsonElement AdcDecodeFromString(String str) {
        String[] adc = str.split(";");
        if (adc.length % 2 != 0)
            return null;

        JsonObject adcData = new JsonObject();
        for (int i = 0; i < adc.length / 2; i++) {
            if (adc[2 * i].equals(String.valueOf(i))) {
                adcData.addProperty(i < infoAdcNames.length ? infoAdcNames[i] : ("Unknown" + i), adc[2 * i + 1]);
//                adcData.add(
//                        new ValueDatum(
//                                i < infoAdcNames.length ? infoAdcNames[i] : "Unknown",
//                                adc[2 * i + 1],
//                                null,
//                                infoAdcUnits[i]
//                        )
//                );
            }
        }
        return adcData;
    }

    JsonElement AdcDecodeFromBinary(byte[] info) {
        if (info.length % 2 != 0)
            if (info.length > 32)
                return null;

        String TabChars = "    ";
        OutputText(TabChars + "Analog value:" + "\r\n");
        TabChars = TabChars + TabChars;

        for (int i = 0; i < info.length / 2; i++) {
            int val = ReadUint16(info, 2 * i);
            byte valId = (byte) ((val >> 12) & 0x000f);
            val &= 0x0fff;
            String strAdc;
            switch (valId) {
                case 0:
                case 2:
                case 3:
                    strAdc = StringFormat("{0}{1}: {2:0.00}{3}--{4}\r\n",
                            TabChars,
                            valId,
                            (double) val * (100 - (-10)) / 4096 + (-10),
                            "(V)",
                            infoAdcNames[i]);
                    break;
                case 1:
                    strAdc = StringFormat("{0}{1}: {2:0.00}{3}--{4}\r\n",
                            TabChars,
                            valId,
                            (double) val * (125 - (-55)) / 4096 + (-55),
                            "(Celsius)",
                            infoAdcNames[i]);
                    break;
                default:
                    strAdc = StringFormat("{0}{1}: {2}--Unknow\r\n",
                            TabChars,
                            valId,
                            val);
                    break;
            }
            OutputText(strAdc);
        }
        return null;
    }

    JsonObject GfsDecodeFromString(String str) {
        JsonObject d = new JsonObject();
        String[] gfs = str.split(";");
        if (gfs.length != 2)
            return null;
        int iStatus = Integer.parseInt(gfs[0], 16);
        int iAlarm = Integer.parseInt(gfs[1], 16);
//        OutputText(TabChars + "Status:\r\n");
//        OutputText("            ");
        for (int i = 0; i < 16; i++) {
            int bitMask = (int) (0x0001 << i);
            d.addProperty("Status" + Utils.formatDecimal(i, 2), (iStatus & bitMask) != 0 ? "I" : "O");
        }
        for (int i = 16; i < 32; i++) {
            int bitMask = (int) (0x0001 << i);
            d.addProperty("Status" + Utils.formatDecimal(i, 2), (iStatus & bitMask) != 0 ? "I" : "O");
        }

        for (int i = 0; i < 16; i++) {
            int bitMask = (int) (0x0001 << i);
            d.addProperty("Alarm" + Utils.formatDecimal(i, 2), (iAlarm & bitMask) != 0 ? "Y" : "N");
        }
        for (int i = 16; i < 32; i++) {
            int bitMask = (int) (0x0001 << i);
            d.addProperty("Alarm" + Utils.formatDecimal(i, 2), (iAlarm & bitMask) != 0 ? "Y" : "N");
        }
        return d;
    }

    JsonElement GfsDecodeFromBinary(byte[] info) {
        String TabChars = "    ";
        if (info.length != 8)
            return null;
        OutputText(TabChars + "Geo-fence:" + "\r\n");
        TabChars = TabChars + TabChars;
        int iStatus = ReadUint32(info, 0);
        int iAlarm = ReadUint32(info, 4);
        OutputText(TabChars + "Status:\r\n");
        OutputText("            ");
        for (int i = 0; i < 16; i++) {
            int bitMask = (int) (0x0001 << i);
            String strStatus = StringFormat("{0:D2}:{1}, ",
                    i,
                    (iStatus & bitMask) != 0 ? "I" : "O");
            OutputText(strStatus);
        }
        OutputText("\r\n");
        OutputText("            ");
        for (int i = 16; i < 32; i++) {
            int bitMask = (int) (0x0001 << i);
            String strStatus = StringFormat("{0:D2}:{1}, ",
                    i,
                    (iStatus & bitMask) != 0 ? "I" : "O");
            OutputText(strStatus);
        }
        OutputText("\r\n");

        OutputText(TabChars + "Alarm:\r\n");
        OutputText("            ");
        for (int i = 0; i < 16; i++) {
            int bitMask = (int) (0x0001 << i);
            String strAlarm = StringFormat("{0:D2}:{1}, ",
                    i,
                    (iAlarm & bitMask) != 0 ? "Y" : "N");
            OutputText(strAlarm);
        }
        OutputText("\r\n");
        OutputText("            ");
        for (int i = 16; i < 32; i++) {
            int bitMask = (int) (0x0001 << i);
            String strAlarm = StringFormat("{0:D2}:{1}, ",
                    i,
                    (iAlarm & bitMask) != 0 ? "Y" : "N");
            OutputText(strAlarm);
        }
        OutputText("\r\n");
        return null;
    }

    JsonElement ObdDecodeFromString(String str) {
        if (str.length() % 2 != 0)
            return null;
        //OutputText("    " + "OBDII:" + "\r\n");

        byte[] obddata = new byte[str.length() / 2];

        for (int i = 0; i < str.length() / 2; i++) {
            String ss = str.substring(2 * i, 2 * i + 2);
            obddata[i] = parseByte(ss, 16);
        }
        return ObdDataDecode(obddata);
    }

    JsonElement ObdDecodeFromBinary(byte[] info) {
        return ObdDataDecode(info);
    }

    JsonElement OalDecodeFromString(String str) {
        if (str.length() % 2 != 0)
            return null;

        byte[] obdalarm = new byte[str.length() / 2];

        for (int i = 0; i < str.length() / 2; i++) {
            obdalarm[i] = parseByte(str.substring(2 * i, 2 * i + 2), 16);
        }
        return ObdDataDecode(obdalarm);
    }

    JsonElement OalDecodeFromBinary(byte[] info) {
        return ObdDataDecode(info);
//        return null;
    }

    JsonElement ObdDataDecode(byte[] obddata) {
        int pos = 0;
        JsonArray d = new JsonArray();

        while (pos < obddata.length) {
            int len = (int) ((obddata[pos] >> 4) & 0x0F);
            if (len + pos > obddata.length)
                break;
            if (len < 3 || len > 8) {
                pos += len;
                continue;
            }
            int service = (int) (obddata[pos] & 0x0f);
            switch (service) {
                case 1:
                case 2: {
                    int pid = obddata[pos + 1];
                    byte[] pidValue = new byte[len - 2];
                    arrayCopy(obddata, pos + 2, pidValue, 0, pidValue.length);
                    d.add(ObdService0102Decode(pidValue, service, pid));
                    break;
                }
                case 3: {
                    byte[] Value = new byte[len - 1];
                    arrayCopy(obddata, pos + 1, Value, 0, Value.length);
                    d.add(ObdService03Decode(Value, service));
                    break;
                }
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
                case 7:
                    break;
                case 8:
                    break;
                case 9:
                    break;
                case 10:
                    break;
                default:
                    break;
            }
            pos += len;
        }
        return d;
    }

    JsonElement FulDecodeFromString(String str, int id) {
        JsonObject o = new JsonObject();
        o.addProperty("id", id);
        o.addProperty("value", Integer.parseInt(str));
        return o;
    }

    JsonElement FulDecodeFromBinary(byte[] info, int id) {
        if (info.length != 4)
            return null;
        int fuel = ReadUint32(info, 0);


        JsonObject o = new JsonObject();
        o.addProperty("id", id);
        o.addProperty("value", fuel);
        return o;
    }

    JsonElement HdbDecodeFromString(String str) {
        byte hdb = 0;
        if (str != null && str.trim().length() > 0) {
            hdb = parseByte(str, 16);
        }
        JsonObject d = new JsonObject();
        for (int i = 0; i < 8; i++) {
            int bitMask = (int) (0x0001 << i);
            d.addProperty(infoBehavior[i], ((hdb & bitMask) != 0) ? 1 : 0);
        }
        return d;
    }

    JsonElement HdbDecodeFromBinary(byte[] info) {
        if (info.length != 1)
            return null;

        byte hdb = ReadUnsignedByte(info, 0);
        if (hdb == 0)
            return null;
        OutputText("    " + "Driver Behavior:" + "\r\n");
        for (int i = 0; i < 8; i++) {
            int bitMask = (int) (0x0001 << i);
            if ((hdb & bitMask) == 0)
                continue;
            String strHdb = StringFormat("            [Bit{0}]--{1}\r\n",
                    i,
                    infoBehavior[i]);
            OutputText(strHdb);
        }
        return null;
    }

    JsonElement CanDecodeFromString(String str) {
        if (str.length() % 2 != 0)
            return null;

        byte[] j1939data = new byte[str.length() / 2];

        for (int i = 0; i < str.length() / 2; i++) {
            j1939data[i] = parseByte(str.substring(2 * i, 2 * i + 2), 16);
        }
        return J1939DataDecode(j1939data);
    }

    JsonElement CanDecodeFromBinary(byte[] info) {
        OutputText("    " + "J1939:" + "\r\n");
        return J1939DataDecode(info);
    }

    JsonElement J1939DataDecode(byte[] candata) {
        int pos = 0;
        JsonArray d = new JsonArray();//"J1939"
        while (pos < candata.length) {
            int len = candata[pos];
            if (len + pos + 1 > candata.length)
                break;
            if (len < 4 || candata[pos + 1] != 0) {
                pos += len + 1;
                continue;
            }

            int pgn = ReadUint16(candata, pos + 2);
            byte[] value = new byte[len - 3];
            arrayCopy(candata, pos + 4, value, 0, value.length);
            J1939PgnDecode(value, pgn, d);
            pos += len + 1;
        }
        return d;
    }

    JsonElement HvdDecodeFromString(String str) {
        if (str.length() % 2 != 0)
            return null;

        byte[] j1708data = new byte[str.length() / 2];

        for (int i = 0; i < str.length() / 2; i++) {
            j1708data[i] = parseByte(str.substring(2 * i, 2 * i + 2), 16);
        }
        return J1708DataDecode(j1708data);
    }

    JsonElement HvdDecodeFromBinary(byte[] info) {
        OutputText("    " + "J1708:" + "\r\n");
        J1708DataDecode(info);
        return null;
    }

    JsonElement J1708DataDecode(byte[] hvddata) {
        int pos = 0;
        JsonArray d = new JsonArray();//"J1708"
        while (pos < hvddata.length) {
            int len = (int) (hvddata[pos] & 0x3F);
            int paratype = (int) ((hvddata[pos] >> 6) & 0x03);
            if (len + pos > hvddata.length)
                break;
            if (len < 2 || len > 22 || paratype == 0) {
                pos += len + 1;
                continue;
            }
            if (paratype == 1)//MID data
            {
                byte[] Value = new byte[len];
                arrayCopy(hvddata, pos + 1, Value, 0, Value.length);
                JsonElement v = J1708MidDecode(Value);
                if (v != null) {
                    d.add(v);
                }
            } else {
                int j1587pid = hvddata[pos + 1];
                if (paratype == 3) {
                    j1587pid += 256;
                }
                byte[] Value = new byte[len - 1];
                arrayCopy(hvddata, pos + 2, Value, 0, Value.length);

                JsonElement v = J1587PidDecode(Value, j1587pid);
                if (v != null) {
                    d.add(v);
                }
            }
            pos += len + 1;
        }
        return d;
    }

    JsonElement VinDecodeFromString(String str) {
        return new JsonPrimitive(str);
//        String strVin = StringFormat("    VIN: {0}\r\n",
//                str);
//        OutputText(strVin);
    }

    JsonElement VinDecodeFromBinary(byte[] info) {
//        String strVin = StringFormat("    VIN: {0}\r\n",
//                new String(info));
//        OutputText(strVin);
        return new JsonPrimitive(new String(info));
    }

    JsonElement RfiDecodeFromString(String str) {
        String[] paras = str.split(";");
        String strRfid = StringFormat("    RFID: {0}({1})\r\n",
                paras[0], Objects.equals(paras[1], "0") ? "Unauthorized" : "Authorized");
//        OutputText(strRfid);
        return new JsonPrimitive(strRfid);
    }

    JsonElement RfiDecodeFromBinary(byte[] info) {
        if (info.length != 11)
            return null;
        String strRfid = StringFormat("    RFID: {0}({1})\r\n",
                new String(info, 0, 10), info[10] == 0 ? "Unauthorized" : "Authorized");
        OutputText(strRfid);
        return null;
    }

    JsonElement EvtDecodeFromString(String str) {
        String[] paras = str.split(";");
        if (paras[0].length() > 2)
            return null;
        if (paras.length == 2 && paras[1].length() > 8)
            return null;
        int eventCode = Integer.parseInt(paras[0], 16);
        int mask = 0;
        if (paras.length == 2) {
            mask = Integer.parseInt(paras[1], 16);
        }
        return EvtCodeStringOut(eventCode, mask);
    }

    JsonElement EvtDecodeFromBinary(byte[] info) {
        if (info.length != 1 && info.length != 5)
            return null;
        Byte eventCode = info[0];
        int mask = 0;
        if (info.length == 5) {
            mask = ReadUint32(info, 1);
        }

        return EvtCodeStringOut(eventCode, mask);
    }

    JsonElement EvtCodeStringOut(int evt, int mask) {
        String eventInfo = "";
        if (evt < 0x10) {
            if (evt < eventInfo0.length) {
                eventInfo = eventInfo0[evt];
            }
        } else if (evt < 0x80) {
            evt -= 0x10;
            if (evt < eventInfo1.length) {
                eventInfo = eventInfo1[evt];
            }
        } else if (evt == 0x80) {
            String strEvent = "";
            int msk = 1;
            for (int i = 0; i < 32; i++) {
                if ((mask & msk) != 0) {
                    strEvent += String.valueOf(i + i) + "|";
                }
                msk <<= 1;
            }
            strEvent = strEvent.substring(0, strEvent.length() - 1);
            return createNameValue("GeoFence status", strEvent);
        } else if (evt == 0x90) {
            String[] infoBehavior = {"Rapid acceleration",
                    "Rough braking",
                    "Harsh course",
                    "No warmup",
                    "Long idle",
                    "Fatigue driving",
                    "Rough terrain",
                    "High RPM"};
            String strEvent = "";
            int msk = 1;
            for (int i = 0; i < 8; i++) {
                if ((mask & msk) != 0) {
                    strEvent += infoBehavior[i] + "|";
                }
                msk <<= 1;
            }
            strEvent = strEvent.substring(0, strEvent.length() - 1);
            return createNameValue("Hash driving", strEvent);

        } else if (evt == 0xE0) {
            String[] infoAlarm = {"Power cut",
                    "Moved",
                    "Over speed",
                    "Jamming",
                    "Geo-fence",
                    "Towing",
                    RESERVED,
                    "Input low",
                    "Input high",
                    RESERVED,
                    "Panic",
                    "OBD",
                    RESERVED,
                    RESERVED,
                    "Accident",
                    "Battery low"};
            String strEvent = "";
            int msk = 1;
            for (int i = 0; i < 16; i++) {
                if ((mask & msk) != 0) {
                    strEvent += infoAlarm[i] + "|";
                }
                msk <<= 1;
            }
            strEvent = strEvent.substring(0, strEvent.length() - 1);
            return createNameValue("Alarm", strEvent);

        } else if (evt == 0xF0) {
            String[] infoStatus = {"Power cut",
                    "Moving",
                    "Over speed",
                    "Jamming",
                    "Geo-fence alarming",
                    "Immobolizer",
                    "ACC",
                    "Input high level",
                    "Input mid level",
                    "Engine",
                    "Panic",
                    "OBD alarm",
                    "Course rapid change",
                    "Speed rapid change",
                    "Roaming",
                    "Inter roaming"};
            String strEvent = "";
            int msk = 1;
            for (int i = 0; i < 16; i++) {
                if ((mask & msk) != 0) {
                    strEvent += infoStatus[i] + "|";
                }
                msk <<= 1;
            }
            if (!Objects.equals(strEvent, ""))
                strEvent = strEvent.substring(0, strEvent.length() - 1);
            return createNameValue("Device status", strEvent);

        }
        if (Objects.equals(eventInfo, "")) {
            eventInfo = "Unknown";
        }
        return createNameValue("Unknown", eventInfo);
    }

    JsonElement BcnDecodeFromString(String str) {
        String[] paras = str.split(";");
        if (paras.length < 2)
            return null;
        int i = 0;
        JsonObject d = new JsonObject();
        JsonArray found = new JsonArray();
        JsonArray lost = new JsonArray();
        d.add("Found", found);
        d.add("Lost", lost);

        while (i < paras.length) {
            if (Objects.equals(paras[i], "1")) {
                for (i = i + 1; i < paras.length; i++) {
                    if (paras[i].length() != 44)
                        break;
                    String uuid = paras[i].substring(0, 32);
                    String major = paras[i].substring(32, 32 + 4);
                    String minor = paras[i].substring(36, 36 + 4);
                    String power = paras[i].substring(40, 40 + 2);
                    String rssi = String.valueOf(Integer.parseInt(paras[i].substring(42, 2), 16));
                    JsonObject g = new JsonObject();
                    g.addProperty("UUID", uuid);
                    g.addProperty("Major", major);
                    g.addProperty("Minor", minor);
                    g.addProperty("Power", power);
                    g.addProperty("RSSI", rssi);
                    found.add(g);
                }
            } else if (Objects.equals(paras[i], "0")) {
                for (i = i + 1; i < paras.length; i++) {
                    if (paras[i].length() != 40)
                        break;
                    String uuid = paras[i].substring(0, 32);
                    String major = paras[i].substring(32, 32 + 4);
                    String minor = paras[i].substring(36, 36 + 4);
                    JsonObject g = new JsonObject();
                    g.addProperty("UUID", uuid);
                    g.addProperty("Major", major);
                    g.addProperty("Minor", minor);
                    lost.add(g);
                }
            } else {
                //return null;
            }
        }
        return d;
    }

    JsonElement BcnDecodeFromBinary(byte[] info) {
        int i = 0;
        while (i < info.length) {
            if ((info[i] & 0x80) == 0)//iBeacon lost
            {
                byte count = (byte) (info[i++] & 0x7F);
                if (info.length < (count * 20 + i))
                    return null;

                int cnt = 0;
                while (cnt < count) {
                    String strUUID = "";
                    for (int j = 0; j < 16; j++) {
                        strUUID += StringFormat("{0:X2}", info[i++]);
                    }
                    int major = ReadUint16(info, i);
                    i += 2;
                    int minor = ReadUint16(info, i);
                    i += 2;
                    String strBeacon = StringFormat("    iBeacon lost UUID:0x{0} Major:0x{1:X0000} Minor:0x{2:X0000}\r\n",
                            strUUID,
                            major,
                            minor);
                    OutputText(strBeacon);
                    cnt++;
                }
            } else                //iBeacon found
            {
                byte count = (byte) (info[i++] & 0x7F);
                if (info.length < (count * 22 + i))
                    return null;
                int cnt = 0;
                while (cnt < count) {
                    String strUUID = "";
                    for (int j = 0; j < 16; j++) {
                        strUUID += StringFormat("{0:X2}", info[i++]);
                    }
                    int major = ReadUint16(info, i);
                    i += 2;
                    int minor = ReadUint16(info, i);
                    i += 2;
                    Byte power = info[i++];
                    Byte rssi = info[i++];
                    String strBeacon = StringFormat("    iBeacon found UUID:0x{0} Major:0x{1:X0000} Minor:0x{2:X0000} Power:0x{3:X0000} RSSI:-{4}dbm\r\n",
                            strUUID,
                            major,
                            minor,
                            power,
                            rssi);
                    OutputText(strBeacon);
                    cnt++;
                }
            }
        }
        return null;
    }

    JsonElement TrpDecodeFromString(String str) {
        String TabChars = "    ";
        String[] trip = str.split(";");
        if (trip.length != 14)
            return null;
        JsonObject d = new JsonObject();
//        OutputText(TabChars + "Trip report:" + "\r\n");
//        TabChars = TabChars + TabChars;

        Date sTime = GetDateTimeFromString(trip[0]);
        d.addProperty("Start Time", Utils.UNIVERSAL_DATETIME_FORMAT.format(sTime));
//        OutputText(StringFormat("{0}Start time: {1}\r\n", TabChars, sTime));

        Date eTime = GetDateTimeFromString(trip[1]);
        d.addProperty("End Time", Utils.UNIVERSAL_DATETIME_FORMAT.format(eTime));
//        OutputText(StringFormat("{0}End time: {1}\r\n", TabChars, eTime));

        d.addProperty("Start Position Latitude", trip[2]);
        d.addProperty("Start Position Longitude", trip[3]);
//        OutputText(StringFormat("{0}Start Position: Lat: {1} Lon: {2}\r\n", TabChars, trip[2], trip[3]));

        d.addProperty("End Position Latitude", trip[4]);
        d.addProperty("End Position Longitude", trip[5]);

//        OutputText(StringFormat("{0}End Position: Lat: {1} Lon: {2}\r\n", TabChars, trip[4], trip[5]));

        d.addProperty("Start Mileage", trip[6]);//, null, "meters"));
//        OutputText(StringFormat("{0}Start mileage: {1}(meters)\r\n", TabChars, trip[6]));

        d.addProperty("End Mileage", trip[7]);//, null, "meters"));
//        OutputText(StringFormat("{0}End mileage: {1}(meters)\r\n", TabChars, trip[7]));

        d.addProperty("Fuel Consumption Cal", trip[8]);
        d.addProperty("Start Fuel Consumption", trip[9]);
        d.addProperty("End Fuel Consumption", trip[10]);
        d.addProperty("Idle Time", trip[11]);//, null, "seconds"));
        d.addProperty("Max Speed", trip[12]);
        d.addProperty("Max RPM", trip[13]);


//        OutputText(StringFormat("{0}Idle time: {1}seconds\r\n", TabChars, trip[11]));

//        OutputText(StringFormat("{0}Max speed: {1}\r\n", TabChars, trip[12]));

//        OutputText(StringFormat("{0}Max RPM: {1}\r\n", TabChars, trip[13]));
        return d;
    }

    JsonElement TrpDecodeFromBinary(byte[] info) {
        if (info.length != 0x31)
            return null;

        String TabChars = "    ";
        OutputText(TabChars + "Trip report:" + "\r\n");
        TabChars = TabChars + TabChars;

        int pos = 0;

        int startTime = ReadUint32(info, pos);
        pos += 4;
        Date timeOffset = DEFAULT_DATE;
        Date sTime = Utils.datePlusSeconds(timeOffset, startTime);
        OutputText(StringFormat("{0}Start time: {1}\r\n", TabChars, sTime));

        int endTime = ReadUint32(info, pos);
        pos += 4;
        Date eTime = Utils.datePlusSeconds(timeOffset, endTime);
        OutputText(StringFormat("{0}End time: {1}\r\n", TabChars, eTime));

        int startLat = ReadInt32(info, pos);
        pos += 4;
        int startLon = ReadInt32(info, pos);
        pos += 4;
        OutputText(StringFormat("{0}Start Position: Lat: {1} Lon: {2}\r\n", TabChars, (double) startLat, (double) startLon));

        int endLat = ReadInt32(info, pos);
        pos += 4;
        int endLon = ReadInt32(info, pos);
        pos += 4;
        OutputText(StringFormat("{0}End Position: Lat: {1} Lon: {2}\r\n", TabChars, (double) endLat, (double) endLon));

        int startMile = ReadUint32(info, pos);
        pos += 4;
        OutputText(StringFormat("{0}Start mileage: {1}(meters)\r\n", TabChars, startMile));

        int endMile = ReadUint32(info, pos);
        pos += 4;
        OutputText(StringFormat("{0}End mileage: {1}(meters)\r\n", TabChars, endMile));

        byte fuelCalId = ReadUnsignedByte(info, pos);
        pos += 1;

        int startFuel = ReadUint32(info, pos);
        pos += 4;
        OutputText(StringFormat("{0}Start fuel consumption(cal:{1}): {2}\r\n", TabChars, fuelCalId, startFuel));

        int endFuel = ReadUint32(info, pos);
        pos += 4;
        OutputText(StringFormat("{0}End fuel consumption(cal:{1}): {2}\r\n", TabChars, fuelCalId, endFuel));

        int idleSec = ReadUint32(info, pos);
        pos += 4;
        OutputText(StringFormat("{0}Idle time: {1}seconds\r\n", TabChars, idleSec));

        int maxSpd = ReadUint16(info, pos);
        pos += 2;
        OutputText(StringFormat("{0}Max speed: {1}\r\n", TabChars, maxSpd));

        int maxRpm = ReadUint16(info, pos);
        pos += 2;
        OutputText(StringFormat("{0}Max RPM: {1}\r\n", TabChars, maxRpm));
        return null;
    }

    JsonElement EgtDecodeFromString(String str) {
//        return new ValueDatum("EGT", str, "Engine seconds", "seconds");
        return new JsonPrimitive(str);
    }

    JsonElement EgtDecodeFromBinary(byte[] info) {
        if (info.length != 4)
            return null;
//        return new ValueDatum("EGT", ReadUint32(info, 0), "Engine seconds", "seconds");
        return new JsonPrimitive(ReadUint32(info, 0));
    }

    JsonElement CmdDecodeFromString(String strCmd, String strResp) {
        JsonObject o = new JsonObject();
        o.addProperty("cmd", strCmd);
        o.addProperty("resp", strResp);
        return o;
    }

    JsonElement ObdService0102Decode(byte[] value, int service, int pid) {
        String str;
        switch (pid) {
            case 0x01: {
                if (value.length != 4)
                    return null;
                JsonObject d = new JsonObject();
                d.addProperty("TYP", "0102");
                d.addProperty("Service", service);
                d.addProperty("PID", pid);

                d.addProperty("DTC_CNT", value[0] & 0x7F);
                d.addProperty("MIL", (value[0] & 0x80) != 0 ? "ON" : "OFF");
                d.addProperty("MIS_SUP", ((value[1] & 0x01) != 0 ? "YES" : "NO"));

                d.addProperty("FUEL_SUP", ((value[1] & 0x02) != 0 ? "YES" : "NO"));
                d.addProperty("CCM_SUP", ((value[1] & 0x04) != 0 ? "YES" : "NO"));
                d.addProperty("MIS_RDY", ((value[1] & 0x10) != 0 ? "YES" : "NO"));
                d.addProperty("FUEL_RDY", ((value[1] & 0x20) != 0 ? "YES" : "NO"));
                d.addProperty("CCM_RDY", ((value[1] & 0x40) != 0 ? "YES" : "NO"));
                d.addProperty("CAT_SUP", ((value[2] & 0x01) != 0 ? "YES" : "NO"));
                d.addProperty("HCAT_SUP", ((value[2] & 0x02) != 0 ? "YES" : "NO"));
                d.addProperty("EVAP_SUP", ((value[2] & 0x04) != 0 ? "YES" : "NO"));
                d.addProperty("AIR_SUP", ((value[2] & 0x08) != 0 ? "YES" : "NO"));
                d.addProperty("ACRF_SUP", ((value[2] & 0x10) != 0 ? "YES" : "NO"));
                d.addProperty("O2S_SUP", ((value[2] & 0x20) != 0 ? "YES" : "NO"));
                d.addProperty("HTR_SUP", ((value[2] & 0x40) != 0 ? "YES" : "NO"));
                d.addProperty("EGR_SUP", ((value[2] & 0x80) != 0 ? "YES" : "NO"));
                d.addProperty("CAT_RDY", ((value[3] & 0x01) != 0 ? "YES" : "NO"));
                d.addProperty("HCAT_RDY", ((value[3] & 0x02) != 0 ? "YES" : "NO"));
                d.addProperty("EVAP_RDY", ((value[3] & 0x04) != 0 ? "YES" : "NO"));
                d.addProperty("AIR_RDY", ((value[3] & 0x08) != 0 ? "YES" : "NO"));
                d.addProperty("ACRF_RDY", ((value[3] & 0x10) != 0 ? "YES" : "NO"));
                d.addProperty("O2S_RDY", ((value[3] & 0x20) != 0 ? "YES" : "NO"));
                d.addProperty("HTR_RDY", ((value[3] & 0x40) != 0 ? "YES" : "NO"));
                d.addProperty("EGR_RDY", ((value[3] & 0x80) != 0 ? "YES" : "NO"));
                return d;
            }
            case 0x04: {
                if (value.length != 1)
                    return null;
                JsonObject d = new JsonObject();
                d.addProperty("TYP", "0102");
                d.addProperty("Service", service);
                d.addProperty("PID", pid);

                double clv = ((double) value[0]) * 100 / 255;
                d.addProperty("Calculated LOAD Value", clv);//, null, "%"));
                return d;
            }
            case 0x05: {
                if (value.length != 1)
                    return null;
                int ect = value[0];
                ect -= 40;
                JsonObject d = new JsonObject();
                d.addProperty("TYP", "0102");
                d.addProperty("Service", service);
                d.addProperty("PID", pid);

                d.addProperty("Engine Coolant Temperature", ect);//, null, "Celsius"));
                return d;
            }
            case 0x06: {
                if (value.length == 1) {

                    JsonObject d = new JsonObject();
                    d.addProperty("TYP", "0102");
                    d.addProperty("Service", service);
                    d.addProperty("PID", pid);

                    d.addProperty("Bank1 Short Term Fuel Trim", (((double) value[0] - 128) / 128) * 100);//, null, "%"));
                    return d;

                } else if (value.length == 2) {
                    JsonObject d = new JsonObject();
                    d.addProperty("TYP", "0102");
                    d.addProperty("Service", service);
                    d.addProperty("PID", pid);

                    d.addProperty("Bank1 Short Term Fuel Trim", (((double) value[0] - 128) / 128) * 100);//, null, "%"));
                    d.addProperty("Bank3 Short Term Fuel Trim", (((double) value[1] - 128) / 128) * 100);//, null, "%"));
                    return d;
                } else
                    return null;
            }
            case 0x07: {
                if (value.length == 1) {
                    JsonObject d = new JsonObject();
                    d.addProperty("TYP", "0102");
                    d.addProperty("Service", service);
                    d.addProperty("PID", pid);

                    d.addProperty("Bank1 Long Term Fuel Trim", (((double) value[0] - 128) / 128) * 100);//, null, "%"));
                    return d;

                } else if (value.length == 2) {
                    JsonObject d = new JsonObject();
                    d.addProperty("TYP", "0102");
                    d.addProperty("Service", service);
                    d.addProperty("PID", pid);

                    d.addProperty("Bank1 Long Term Fuel Trim", (((double) value[0] - 128) / 128) * 100);//, null, "%"));
                    d.addProperty("Bank3 Long Term Fuel Trim", (((double) value[1] - 128) / 128) * 100);//, null, "%"));
                    return d;
                } else
                    return null;
            }
            case 0x08: {
                if (value.length == 1) {
                    JsonObject d = new JsonObject();
                    d.addProperty("TYP", "0102");
                    d.addProperty("Service", service);
                    d.addProperty("PID", pid);

                    d.addProperty("Bank2 Short Term Fuel Trim", (((double) value[0] - 128) / 128) * 100);//, null, "%"));
                    return d;
                } else if (value.length == 2) {
                    JsonObject d = new JsonObject();
                    d.addProperty("TYP", "0102");
                    d.addProperty("Service", service);
                    d.addProperty("PID", pid);

                    d.addProperty("Bank2 Short Term Fuel Trim", (((double) value[0] - 128) / 128) * 100);//, null, "%"));
                    d.addProperty("Bank4 Short Term Fuel Trim", (((double) value[1] - 128) / 128) * 100);//, null, "%"));
                    return d;
                } else
                    return null;
            }
            case 0x09: {
                if (value.length == 1) {
                    JsonObject d = new JsonObject();
                    d.addProperty("TYP", "0102");
                    d.addProperty("Service", service);
                    d.addProperty("PID", pid);

                    d.addProperty("Bank2 Long Term Fuel Trim", (((double) value[0] - 128) / 128) * 100);//, null, "%"));
                    return d;
                } else if (value.length == 2) {
                    JsonObject d = new JsonObject();
                    d.addProperty("TYP", "0102");
                    d.addProperty("Service", service);
                    d.addProperty("PID", pid);

                    d.addProperty("Bank2 Long Term Fuel Trim", (((double) value[0] - 128) / 128) * 100);//, null, "%"));
                    d.addProperty("Bank4 Long Term Fuel Trim", (((double) value[1] - 128) / 128) * 100);//, null, "%"));
                    return d;
                } else
                    return null;
            }
            case 0x0A: {
                if (value.length != 1)
                    return null;
                JsonObject d = new JsonObject();
                d.addProperty("TYP", "0102");
                d.addProperty("Service", service);
                d.addProperty("PID", pid);

                d.addProperty("Fuel Rail Pressure", (int) value[0] * 3);//, null, "kPa"));
                return d;
            }
            case 0x0B: {
                if (value.length != 1)
                    return null;
                JsonObject d = new JsonObject();
                d.addProperty("TYP", "0102");
                d.addProperty("Service", service);
                d.addProperty("PID", pid);

                d.addProperty("Intake Manifold Absolute Pressure", value[0]);//, null, "kPa"));
                return d;
            }
            case 0x0C: {
                if (value.length != 2)
                    return null;
                double rpm = ((double) (value[0] * 256 + value[1])) / 4;
                JsonObject d = new JsonObject();
                d.addProperty("TYP", "0102");
                d.addProperty("Service", service);
                d.addProperty("PID", pid);

                d.addProperty("Engine RPM", rpm);//, null, "rpm"));
                return d;
            }
            case 0x0D: {
                if (value.length != 1)
                    return null;
                int speed = value[0];
                JsonObject d = new JsonObject();
                d.addProperty("TYP", "0102");
                d.addProperty("Service", service);
                d.addProperty("PID", pid);

                d.addProperty("Vehicle Speed", speed);//, null, "km/h"));
                return d;
            }
            case 0x0E: {
                if (value.length != 1)
                    return null;
                JsonObject d = new JsonObject();
                d.addProperty("TYP", "0102");
                d.addProperty("Service", service);
                d.addProperty("PID", pid);

                d.addProperty("Ignition Timing Advance for #1 Cylinder", ((double) value[0] - 128) / 2);//, null, "degree"));
                return d;
            }
            case 0x0F: {
                if (value.length != 1)
                    return null;
                int iat = value[0];
                iat -= 40;
                JsonObject d = new JsonObject();
                d.addProperty("TYP", "0102");
                d.addProperty("Service", service);//));
                d.addProperty("PID", pid);//));

                d.addProperty("Intake Air Temperature", iat);//, null, "Celsius"));
                return d;
            }
            case 0x10: {
                if (value.length != 2)
                    return null;
                double maf = ((double) (value[0] * 256 + value[1])) / 100;
                JsonObject d = new JsonObject();
                d.addProperty("TYP", "0102");
                d.addProperty("Service", service);
                d.addProperty("PID", pid);

                d.addProperty("Air Flow Rate from Mass Air Flow Sensor", maf);//, null, "g/s"));
                return d;
            }
            case 0x11: {
                if (value.length != 1)
                    return null;
                double position = ((double) value[0]) * 100 / 255;

                JsonObject d = new JsonObject();
                d.addProperty("TYP", "0102");
                d.addProperty("Service", service);
                d.addProperty("PID", pid);

                d.addProperty("Absolute Throttle Position", position);//, null, "%"));
                return d;
            }
            case 0x21: {
                if (value.length != 2)
                    return null;
                int distance = 256 * value[0] + value[1];
                JsonObject d = new JsonObject();
                d.addProperty("TYP", "0102");
                d.addProperty("Service", service);
                d.addProperty("PID", pid);

                d.addProperty("Distance Travelled While MIL is Activated", distance);//, null, "km"));
                return d;
            }
            case 0x2F: {
                if (value.length != 1)
                    return null;
                double percent = ((double) value[0]) * 100 / 255;
                JsonObject d = new JsonObject();
                d.addProperty("TYP", "0102");
                d.addProperty("Service", service);
                d.addProperty("PID", pid);

                d.addProperty("Fuel level", percent);//, null, "%"));
                return d;
            }
            case 0x31: {
                if (value.length != 2)
                    return null;
                int distance = 256 * value[0] + value[1];
                JsonObject d = new JsonObject();
                d.addProperty("TYP", "0102");
                d.addProperty("Service", service);
                d.addProperty("PID", pid);

                d.addProperty("Distance traveled since codes cleared", distance);//, null, "km"));
                return d;
            }
            default: {
                String hex = DatatypeConverter.printHexBinary(value);
                JsonObject d = new JsonObject();
                d.addProperty("TYP", "0102");
                d.addProperty("Service", service);
                d.addProperty("PID", pid);

                d.addProperty("raw", hex);
                return d;
            }
        }
    }

    JsonElement ObdService03Decode(byte[] value, int service) {
        int offset;
        if (value.length % 2 != 0)
            offset = 1;
        else
            offset = 0;
        String strDtcs = "";
        String[] dtcChars = {"P", "C", "B", "U"};
        for (int i = 0; i < value.length / 2; i++) {
            byte dtcA = value[2 * i + offset];
            byte dtcB = value[2 * i + offset + 1];
            if (dtcA == 0 && dtcB == 0)
                continue;
            String a1 = dtcChars[((dtcA >> 6) & 0x03)];
            int a2 = dtcA & 0x3F;
            strDtcs += a1 + Utils.formatHex(a2, 2) + Utils.formatHex(dtcB & 0xFF, 2) + "/";

        }
        strDtcs = strDtcs.substring(0, strDtcs.length() - 1);
        JsonObject d = new JsonObject();//"OBD-03"
        d.addProperty("Service", service);
        d.addProperty("ServiceName", "03");

        d.addProperty("raw", strDtcs);
        return d;
    }

    JsonObject createNameValue(String n, String v) {
        JsonObject o = new JsonObject();
        o.addProperty("name", v);
        return o;
    }

    JsonObject createNameValue(String n, double v) {
        JsonObject o = new JsonObject();
        o.addProperty("name", v);
        return o;
    }

    JsonObject createNameValue(String n, int v) {
        JsonObject o = new JsonObject();
        o.addProperty("name", v);
        return o;
    }

    void J1939PgnDecode(byte[] value, int pgn, JsonArray d) {
        switch (pgn) {
            case 61444://(0x00F004)Engine speed
                d.add(createNameValue("PGN" + pgn, (double) ReadUint16(value, 3) * 0.125));//, "Engine speed", "RPM"));
                return;
            case 65132://(0x00FE6C)Vehicle speed
                d.add(createNameValue("PGN" + pgn, (double) ReadUint16(value, 6) / 256));//, "Vehicle speed", "km/h"));
                return;
            case 65217://(0x00FEC1)High Resolution Total Vehicle Distance
                d.add(createNameValue("PGN" + pgn, (double) ReadUint32(value, 0) / 200));//, "High Resolution Total Vehicle Distance", "km"));
                return;
            case 65248:
                d.add(createNameValue("PGN" + pgn, (double) ReadUint32(value, 0) / 8));//, "Trip Distance", "km"));
                d.add(createNameValue("PGN" + pgn, (double) ReadUint32(value, 4) / 8));//, "Total Vehicle Distance", "km"));
                return;
            case 65262://(0x00FEEE)Engine Coolant Temperature
                d.add(createNameValue("PGN" + pgn, (double) ReadUnsignedByte(value, 0) - 40));//, "Engine Coolant Temperature", "deg C"));
                return;
            case 65257://(0x00FEE9)Fuel consumption
                d.add(createNameValue("PGN" + pgn, (double) ReadUint16(value, 4) * 0.5));//, "Total fuel used", "L"));
                return;
            case 61443://(0x00F003)Accelerator Pedal Position
                d.add(createNameValue("PGN" + pgn, (double) ReadUnsignedByte(value, 1) * 0.4));//, "Accelerator Pedal Position", "%"));
                return;
            case 65226://(DM1)Active DTCs and lamp status information
                J1939DtcsDecode(value, d, "PGN" + pgn + " Active DTC ", "(DM1)Active DTCs and lamp status information");
                return;
            case 65227://(DM2)Previously active DTCs and lamp status information
                J1939DtcsDecode(value, d, "PGN" + pgn + " Previously Active DTC ", "(DM2)Previously active DTCs and lamp status information");
                return;
            default:
                String hex = DatatypeConverter.printHexBinary(value);
                d.add(createNameValue("PGN" + pgn, hex));//, "Raw", null));
                return;
        }
    }

    String J1939DtcsDecode(byte[] value, JsonArray d, String shortPrefix, String prefix) {
        String str = "";
        if (value.length < 7)
            return str;
        String[] LampStatus = {"OFF", "ON", "Unknown", "Unknown"};
        d.add(createNameValue(shortPrefix + "MIL", LampStatus[(value[0] >> 6) & 0x03]));//, prefix + " " + "MIL"));//, null));
        d.add(createNameValue(shortPrefix + "RSL", LampStatus[(value[0] >> 4) & 0x03]));//, prefix + " " + "RSL"));//, null));
        d.add(createNameValue(shortPrefix + "AWL", LampStatus[(value[0] >> 2) & 0x03]));//, prefix + " " + "AWL"));//, null));
        d.add(createNameValue(shortPrefix + "PL", LampStatus[value[0] & 0x03]));//, prefix + " " + "PL"));//, null));


        for (int i = 0; i < (value.length - 2) / 4; i++) {
            int Dtc = ReadUint32(value, i * 4 + 2);
            d.add(createNameValue(shortPrefix + "DTC" + i + " SPN", (Dtc >> 13) & 0x7FFFF));//, null, null));
            d.add(createNameValue(shortPrefix + "DTC" + i + " FMI", (Dtc >> 8) & 0x1F));//, null, null));
            d.add(createNameValue(shortPrefix + "DTC" + i + " OC", Dtc & 0x7F));//, null, null));
        }
        return str;
    }

    JsonElement J1708MidDecode(byte[] value) {
        String hex = "";
        for (int i = 1; i < value.length; i++) {
            hex += Utils.formatHex(value[i], 2) + " ";
        }
        JsonObject o = new JsonObject();
        o.addProperty("TYP", "J1708");
        o.addProperty("MID", hex);
        return o;//, null, null));
    }

    JsonElement J1587PidDecode(byte[] value, int pid) {
        String strPID;
        JsonObject d = null;
        switch (pid) {
            case 84://Road speed
                d = new JsonObject();
                d.addProperty("TYP", "J1587");
                d.addProperty("PID", pid);
                d.addProperty("PID" + pid, (double) ReadUnsignedByte(value, 0) * 0.805);//, "Road speed", "km/h"));
                return d;
            case 96://Fuel level
            {
                d = new JsonObject();
                d.addProperty("TYP", "J1587");
                d.addProperty("PID", pid);
                d.addProperty("PID" + pid, (double) ReadUnsignedByte(value, 0) * 0.5);//, "Fuel level", "%"));
                return d;
            }
            case 110://Engine Coolant Temperature
            {
                byte v = ReadUnsignedByte(value, 0);
                d = new JsonObject();
                d.addProperty("TYP", "J1587");
                d.addProperty("PID", pid);//));
                d.addProperty("PID" + pid, v);//, "Engine Coolant Temperature", "Fahrenheit"));
                return d;
            }
            case 190://Engine speed
            {
                double v = (double) ReadUint16(value, 0) * 0.25;
                d = new JsonObject();
                d.addProperty("TYP", "J1587");
                d.addProperty("PID", pid);//));
                d.addProperty("PID" + pid, v);//, "Engine speed", "RPM"));
                return d;
            }
            case 245://Total Vehicle Distance
            {
                double v = (double) ReadUint32(value, 0) * 0.161;
                d = new JsonObject();
                d.addProperty("TYP", "J1587");
                d.addProperty("PID", pid);//));
                d.addProperty("PID" + pid, v);//, "Total Vehicle Distance", "km"));
                return d;
            }
            default: {
                String hex = DatatypeConverter.printHexBinary(value);
                d = new JsonObject();
                d.addProperty("TYP", "J1587");
                d.addProperty("PID", pid);//));
                d.addProperty("PID" + pid, hex);//, null, null));
                return d;
            }
        }
    }

    String DataTxtAcknowledgement(byte[] data) {
        return ("*TS01,ACK:" + Utils.formatHex(GetCrc16Value(data, data.length), 4) + "#");
    }

    byte[] DataBinAcknowledgement(byte[] data) {
        int crcData = GetCrc16Value(data, data.length);
        byte[] ackData = new byte[6];

        ackData[0] = ProtocolVersion;
        ackData[1] = AckFlag;
        ackData[2] = (byte) ((crcData >> 8) & 0xFF);
        ackData[3] = (byte) (crcData & 0xFF);
        int crcFrame = GetCrc16Value(ackData, ackData.length - 2);
        ackData[4] = (byte) ((crcFrame >> 8) & 0xFF);
        ackData[5] = (byte) (crcFrame & 0xFF);

        return binDataPacket(ackData);
    }

    byte[] binDataPacket(byte[] data) {
        ByteArrayList packet = new ByteArrayList();

        packet.add(binFlagChar);
        for (int i = 0; i < 6; i++) {
            if (data[i] == binFlagChar || data[i] == binEscapeChar) {
                packet.add(binEscapeChar);
                packet.add((byte) ((data[i] ^ binEscapeChar) & 0xFF));
            } else {
                packet.add(data[i]);
            }
        }
        packet.add(binFlagChar);
        return packet.toArray();
    }

    int GetCrc16Value(byte[] dat, int length) {
        int[] crc_ta = {0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7,
                0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef,
                0x1231, 0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6,
                0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de,
                0x2462, 0x3443, 0x0420, 0x1401, 0x64e6, 0x74c7, 0x44a4, 0x5485,
                0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d,
                0x3653, 0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4,
                0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe, 0xd79d, 0xc7bc,
                0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823,
                0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b,
                0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12,
                0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a,
                0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22, 0x3c03, 0x0c60, 0x1c41,
                0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49,
                0x7e97, 0x6eb6, 0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0x0e70,
                0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59, 0x8f78,
                0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f,
                0x1080, 0x00a1, 0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067,
                0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e,
                0x02b1, 0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256,
                0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d,
                0x34e2, 0x24c3, 0x14a0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
                0xa7db, 0xb7fa, 0x8799, 0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c,
                0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634,
                0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab,
                0x5844, 0x4865, 0x7806, 0x6827, 0x18c0, 0x08e1, 0x3882, 0x28a3,
                0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a,
                0x4a75, 0x5a54, 0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92,
                0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9,
                0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1,
                0xef1f, 0xff3e, 0xcf5d, 0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8,
                0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0};

        int crc;
        byte da;

        crc = 0;
        for (int i = 0; i < length; i++) {
            da = (byte) (crc >> 8);
            crc <<= 8;
            crc ^= crc_ta[da ^ dat[i]];
        }
        return crc;
    }

    byte ReadUnsignedByte(byte[] dat, int pos) {
        if (pos + 1 > dat.length)
            return 0;
        return dat[pos];
    }

//    Int16 ReadInt16(byte[] dat, int pos)
//    {
//        return (Int16)ReadUint16(dat, pos);
//    }

    byte ReadSignedByte(byte[] dat, int pos) {
        if (pos + 1 > dat.length)
            return 0;
        return (byte) dat[pos];
    }

    int ReadUint32(byte[] dat, int pos) {
        if (pos + 4 > dat.length)
            return 0;
        int val = 0;
        for (int i = 0; i < 4; i++) {
            val = (val << 8) + dat[pos + i];
        }
        return val;
    }

    int ReadInt32(byte[] dat, int pos) {
        return (int) ReadUint32(dat, pos);
    }

    int ReadUint16(byte[] dat, int pos) {
        if (pos + 2 > dat.length)
            return 0;
        int val = 0;
        for (int i = 0; i < 2; i++) {
            val = (int) ((val << 8) + dat[pos + i]);
        }
        return val;
    }

    void OutputText(String str) {
        System.out.println("---->> " + str);
//        tempWrite.Write(str);
//        tempWrite.Flush();
//        tempFile.Flush();
    }

    public interface FrameListener {
        void onReadFrame(JsonObject f);
    }
}
