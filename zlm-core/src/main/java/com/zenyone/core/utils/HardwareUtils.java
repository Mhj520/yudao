package com.zenyone.core.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HardwareUtils {

    private static final Logger logger = LoggerFactory.getLogger(HardwareUtils.class);

    public static String getHardwareId() {
        String cpuSerial = getCPUSerial();
        String macAddress = getMacAddress();
        String diskSerial = getDiskSerial();
        String motherboardSerial = getMotherboardSerial();
        if (cpuSerial != null && macAddress != null && diskSerial != null && motherboardSerial != null) {
            String source = cpuSerial + macAddress + diskSerial + motherboardSerial;
            return hash(source);

        } else if (macAddress != null && diskSerial != null) {
            String source = macAddress + diskSerial;
            return hash(source);

        } else if (macAddress != null) {
            return hash(macAddress);
        } else {
            return null;
        }
    }

    private static String hash(String source) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to create message digest", e);
            return null;
        }
    }

    private static String getMacAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (!networkInterface.isLoopback() && !networkInterface.isVirtual() &&
                    !networkInterface.getName().toLowerCase().contains("docker") && !networkInterface.getName().toLowerCase().contains("vmware")) {
                     byte[] mac = networkInterface.getHardwareAddress();
                    if (mac != null) {
                       StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < mac.length; i++) {
                            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                        }
                        return sb.toString();
                    }
                }
            }
             logger.warn("Failed to get MAC Address. using mock mac Address ");
              return "mockMacAddress";
        } catch (Exception e) {
            logger.error("Failed to get MAC Address.", e);
           return "mockMacAddress";
        }
    }

     private static String getDiskSerial() {
      //  模拟获取硬盘序列号，请替换为实际的获取方法
        try{
            Process process = Runtime.getRuntime().exec("wmic diskdrive get SerialNumber");
            process.getOutputStream().close();
            Scanner scanner = new Scanner(process.getInputStream());
            if (scanner.hasNext()){
                scanner.next();
            }
            if(scanner.hasNext()){
                 String diskSerial =  scanner.next().trim();
               scanner.close();
                return diskSerial;
            }
            scanner.close();
             logger.warn("Failed to get Disk serial number. using mock disk serial ");
           return "mockDiskSerial";
        }catch(Exception e){
             logger.error("Failed to get Disk serial number.",e);
             return  "mockDiskSerial";
        }
    }

    private static String getMotherboardSerial() {
        //  模拟获取主板序列号，请替换为实际的获取方法
        try{
           Process process = Runtime.getRuntime().exec("wmic baseboard get serialnumber");
             process.getOutputStream().close();
            Scanner scanner = new Scanner(process.getInputStream());
            if (scanner.hasNext()){
                 scanner.next();
            }
            if(scanner.hasNext()){
                String motherboardSerial  = scanner.next().trim();
                  scanner.close();
                return motherboardSerial;

            }
              scanner.close();
             logger.warn("Failed to get motherboard serial number. using mock motherboard serial ");
           return "mockMotherboardSerial";

        }catch (Exception e){
            logger.error("Failed to get motherboard serial number.",e);
           return  "mockMotherboardSerial";
       }
    }

    private static String getCPUSerial() {
        String serialNumber = null;
        try {
            serialNumber = getCPUSerialByWmic();
            if (serialNumber != null && !serialNumber.isEmpty()) {
                return serialNumber;
            }
            serialNumber = getCPUSerialByVBS();
            if (serialNumber == null || serialNumber.isEmpty()) {
                logger.error("Failed to get CPU serial number by both wmic and vbscript");
            }
            return serialNumber;
        } catch (Exception e) {
            logger.error("Failed to get CPU serial number", e);
            return null;
        }
    }

    private static String getCPUSerialByWmic() throws Exception {
        String serialNumber = "";
        Process process = Runtime.getRuntime().exec("wmic cpu get processorid");
        process.getOutputStream().close();
        Scanner scanner = new Scanner(process.getInputStream());
        if (scanner.hasNext()) {
            scanner.next();
        }
        if (scanner.hasNext()) {
            serialNumber = scanner.next().trim();
        }
        scanner.close();
        if (serialNumber == null || serialNumber.isEmpty()) {
            logger.warn("Failed to get CPU serial number by wmic.");
            return null;
        }
        return serialNumber;
    }

    private static String getCPUSerialByVBS() throws Exception {
        String result = "";
        File file = null;
        try {
            file = File.createTempFile("tmp", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new FileWriter(file);
            String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                    + "Set colItems = objWMIService.ExecQuery _ \n" + "   (\"Select * from Win32_Processor\") \n"
                    + "For Each objItem in colItems \n" + "    Wscript.Echo objItem.ProcessorId \n"
                    + "    exit for  ' do the first cpu only! \n" + "Next \n";
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
            file.delete();
            if (result == null || result.isEmpty()) {
                logger.warn("Failed to get CPU serial number by vbscript.");
                return null;
            }

        } catch (Exception e) {
            logger.error("Failed to get CPU serial number by vbscript", e);
            if (file != null) {
                file.delete();
            }
            return null;
        }
        return result.trim();
    }

    public static void main(String[] args) {
        String hardwareId = getHardwareId();
        System.out.println("Hardware Id: " + hardwareId);
    }
}