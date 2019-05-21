/**
 * @author qinxue.pan E-mail: xue@acrcloud.com
 * @version 1.0.0
 * @create 2015.10.01
 **/

package com.acrcloud.utils;

/*
 
ACRCloudExtrTool Copyright 2015 ACRCloud v1.0.0

     This module can create "ACRCloud Fingerprint" from most of audio/video file.(
       *          Audio: mp3, mp4, wav, m4a, aac, amr, ape, flv, flac, ogg, wma, caf, alac).

 Functions:
     createFingerprintByFile(file_name, start_time_seconds, audio_len_seconds, is_db_fingerprint);
         file_name: Path of input file; 
         start_time_seconds: Start time of input file, default is 0; 
         audio_len_seconds: Length of audio data you need. if you create recogize frigerprint, default is 12 seconds, if you create db frigerprint, it is not usefully; 
         is_db_fingerprint: If it is True, it will create db frigerprint; 

     createFingerprintByFileBuffer(data_buffer, data_buffer_len, start_time_seconds, audio_len_seconds, is_db_fingerprint);
         data_buffer: data buffer of input file; 
         start_time_seconds: Start time of input file, default is 0; 
         audio_len_seconds: Length of audio data you need. if you create recogize frigerprint, default is 12 seconds, if you create db frigerprint, it is not usefully; 
         is_db_fingerprint: If it is True, it will create db frigerprint; 

     decodeAudioByFile(file_name, start_time_seconds, audio_len_seconds);
         It will return the audio data(RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16 bit, mono 8000 Hz); 
             file_name: Path of input file; 
             start_time_seconds: Start time of input file, default is 0; 
             audio_len_seconds: Length of audio data you need, if it is 0, will decode all the audio; 

     decodeAudioByFileBuffer(data_buffer, data_buffer_len, start_time_seconds, audio_len_seconds);
         It will return the audio data(RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16 bit, mono 8000 Hz); 
             data_buffer: data buffer of input file; 
             start_time_seconds: Start time of input file, default is 0; 
             audio_len_seconds: Length of audio data you need, if it is 0, will decode all the audio; 

     get_doc() 
         return the doc of this module 

     set_debug() 
         set debug, and print all info.

*/
public class ACRCloudExtrTool {

    static {
        try {
            System.loadLibrary("ACRCloudExtrTool");
            native_init();
        } catch (Exception e) {
            System.err.println("ACRCloudExtrTool loadLibrary error!");
        }
    }

    public ACRCloudExtrTool() {
    }

    public static byte[] createFingerprintByFile(String fileName, int startTimeSeconds, int audioLenSeconds, boolean isDB) {
        if (fileName == null || "".equals(fileName)) {
            return null;
        }
        return native_create_fingerprint_by_file(fileName, startTimeSeconds, audioLenSeconds, isDB);
    }

    public static byte[] createFingerprintByFileBuffer(byte[] dataBuffer, int dataBufferLen, int startTimeSeconds, int audioLenSeconds, boolean isDB) {
        if (dataBuffer == null || dataBufferLen <= 0) {
            return null;
        }
        return native_create_fingerprint_by_filebuffer(dataBuffer, dataBufferLen, startTimeSeconds, audioLenSeconds, isDB);
    }

    public static byte[] createFingerprint(byte[] dataBuffer, int dataBufferLen, boolean isDB) {
        if (dataBuffer == null || dataBufferLen <= 0) {
            return null;
        }
        return native_create_fingerprint(dataBuffer, dataBufferLen, isDB);
    }

    public static byte[] decodeAudioByFile(String fileName, int startTimeSeconds, int audioLenSeconds) {
        if (fileName == null || "".equals(fileName)) {
            return null;
        }
        return native_decode_audio_by_file(fileName, startTimeSeconds, audioLenSeconds);
    }

    public static byte[] decodeAudioByFileBuffer(byte[] dataBuffer, int dataBufferLen, int startTimeSeconds, int audioLenSeconds) {
        if (dataBuffer == null || dataBufferLen <= 0) {
            return null;
        }
        return native_decode_audio_by_filebuffer(dataBuffer, dataBufferLen, startTimeSeconds, audioLenSeconds);
    }

    public static void setDebug() {
        native_set_debug();
    }

    public static String getDoc() {
        return native_get_doc();
    }

    private static native void native_init();

    private static native byte[] native_create_fingerprint_by_file(String file_name, int start_time_seconds, int audio_len_seconds, boolean is_db_fingerprint);

    private static native byte[] native_create_fingerprint_by_filebuffer(byte[] data_buffer, int data_buffer_len, int start_time_seconds, int audio_len_seconds, boolean is_db_fingerprint);

    private static native byte[] native_create_fingerprint(byte[] wav_data_buffer, int wav_data_buffer_len, boolean is_db_fingerprint);

    private static native byte[] native_decode_audio_by_file(String file_name, int start_time_seconds, int audio_len_seconds);

    private static native byte[] native_decode_audio_by_filebuffer(byte[] data_buffer, int data_buffer_len, int start_time_seconds, int audio_len_seconds);

    private static native void native_set_debug();

    private static native String native_get_doc();
}
