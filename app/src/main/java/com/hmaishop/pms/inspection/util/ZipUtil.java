package com.hmaishop.pms.inspection.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 打包文件的工具类
 * <p>
 * Created by Joker_Runner on 8/1 0001.
 */
public class ZipUtil {
    public static void ZipFolder(String tozipFolder, String savezipPath) throws Exception {
        // ZIP 输出流
        ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(savezipPath));
        // 待压缩文件或目录
        File file = new File(tozipFolder);
        // 压缩
        ZipFiles(file.getParent() + File.separator, file.getName(), outZip);
        //finish and close
        outZip.finish();
        outZip.close();
    }

    private static void ZipFiles(String folderString, String fileString, ZipOutputStream zipOutputSteam) throws Exception {
        if (zipOutputSteam == null)
            return;
        File file = new File(folderString + fileString);
        if (file.isFile()) {
            ZipEntry zipEntry = new ZipEntry(fileString);
            FileInputStream inputStream = new FileInputStream(file);
            zipOutputSteam.putNextEntry(zipEntry);
            int len;
            byte[] buffer = new byte[4096];
            while ((len = inputStream.read(buffer)) != -1) {
                zipOutputSteam.write(buffer, 0, len);
            }
            zipOutputSteam.closeEntry();
        } else {
            // 是文件
            String[] fileList = file.list();
            //no child file and compress
            if (fileList == null || fileList.length <= 0) {
                ZipEntry zipEntry = new ZipEntry(fileString + File.separator);
                zipOutputSteam.putNextEntry(zipEntry);
                zipOutputSteam.closeEntry();
            }
            // child files and recursion
            for (int i = 0; i < fileList.length; i++) {
                ZipFiles(folderString, fileString + java.io.File.separator + fileList[i], zipOutputSteam);
            }
        }
    }

}