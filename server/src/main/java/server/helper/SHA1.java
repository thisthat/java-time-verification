package server.helper;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1 {
    /**
     * Read the file and calculate the SHA-1 checksum
     * Thank to: user2084795
     * https://stackoverflow.com/questions/6293713/java-how-to-create-sha-1-for-a-file
     *
     * @param file
     *            the file to read
     * @return the hex representation of the SHA-1 using uppercase chars
     * @throws NoSuchAlgorithmException
     *             should never happen
     *
     */
    public static String calcate(File file) {

        MessageDigest sha1 = null;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            //not the case
        }
        try (InputStream input = new FileInputStream(file)) {

            byte[] buffer = new byte[8192];
            int len = input.read(buffer);

            while (len != -1) {
                sha1.update(buffer, 0, len);
                len = input.read(buffer);
            }
            byte[] digest = sha1.digest();
            String s = byteArrayToHexString(digest).toUpperCase();
            return s;
        } catch (Exception e) {
            return null;
        }
    }

    private static String byteArrayToHexString(byte[] b) {
        StringBuilder result = new StringBuilder();
        for (int i=0; i < b.length; i++) {
            result.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    /**
     * @see SHA1#calcate(File)
     * @param file
     *              path of the file
     * @return @see SHA1#calcate(File)
     * @throws NoSuchAlgorithmException
     */
    public static String calcate(String file) {
        return calcate(new File(file));
    }
}
