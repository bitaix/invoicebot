package de.bitaix.invoicbot.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileSaver {

  public Optional<String> saveFile(InputStream inputStream, String fileName,
      String currentRootDir) {
    try {
      var file = createFile(fileName, currentRootDir);
      file.getParentFile().mkdirs();
      try (FileOutputStream outputStream = new FileOutputStream(file)) {
        var buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
        log.info("Save File to : {}", file.getAbsolutePath());
      }
      return Optional.ofNullable(file.getAbsolutePath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static File createFile(String fileName, String currentRootDir) {
    var file = new File(currentRootDir + fileName);
    if (!file.exists()) {
      return file;
    }
    var newFileName = fileName.replace(".", "_" + System.currentTimeMillis() + ".");
    log.info("File already exists. Renaming file to {}", newFileName);
    return new File(currentRootDir + newFileName);
  }
}
