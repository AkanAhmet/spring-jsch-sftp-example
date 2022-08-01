package com.akan.spring_jsch_sftp;

import com.jcraft.jsch.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

/**
 * @Author Ahmet AKAN
 * @Date 01-Aug-22 02:01 PM
 */

@Service
@Log4j2
public class SftpService {

    String remoteHost = "sftp_IP";
    String username = "sftp_user";
    String password = "sftp_pass";
    int port = 22;
    File file;

    @PostConstruct
    public void sendFile() throws IOException {

        // Connection informations

        int port = 22;

        ChannelSftp channelSftp = null;
        Session jschSession = null;
        String remoteFileName = "file.txt";
        String dir = "sentFromRemote";


            JSch jsch = new JSch();
            jschSession = null;
            //jsch.setKnownHosts("~/.ssh/known_hosts");
            try {

                File file = new File("file.txt");
                file.createNewFile();
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write("Spring Jsch - Sftp Example");
                fileWriter.flush();

                //!! JSCH Session Connection Settings
                jschSession = jsch.getSession(username, remoteHost, port);
                jschSession.setPassword(password);
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                config.put("PreferredAuthentications", "password");
                jschSession.setConfig(config);
                jschSession.connect();

            //!! Sftp Channel Settings
            channelSftp = (ChannelSftp) jschSession.openChannel("sftp");
            channelSftp.connect();
            channelSftp.cd(username);

            // Create folder on remote pc if not exists.
            boolean dirAlreadyExists = false;
            Vector vector = channelSftp.ls(channelSftp.pwd());
            Iterator iterator = vector.iterator();
            while(iterator.hasNext()) {
                if(String.valueOf(iterator.next()).contains(dir)){
                    dirAlreadyExists = true;
                    break;
                }
            }

            if (!dirAlreadyExists) {
                channelSftp.mkdir(dir);
            }

            channelSftp.put(new FileInputStream(file) ,dir + "/" + remoteFileName);

        } catch (JSchException jSchException) {
            log.error("SftpService -> sendFile -> JSchException !" + jSchException.getLocalizedMessage());

        } catch (SftpException sftpException) {
            log.error("SftpService -> sendFile -> SftpException !" + sftpException.getLocalizedMessage());

        } catch (RuntimeException runtimeException) {
            log.error("SftpService -> sendFile -> RuntimeException !" + runtimeException.getLocalizedMessage());

        } finally {

            if (jschSession != null) { jschSession.disconnect(); }
            if (channelSftp != null) {
                channelSftp.disconnect();
                channelSftp.exit();
            }
        }
    }

}
