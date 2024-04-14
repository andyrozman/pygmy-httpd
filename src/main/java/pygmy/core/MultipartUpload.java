package pygmy.core;

import java.io.*;
import java.util.Map;

public class MultipartUpload {

    private String name;
    private String filename;
    private String contentType;
    private File cachedUpload;

    public MultipartUpload(HttpRequest request, InternetInputStream stream, Map<String, String> formHeaders, String boundary, String charset) throws IOException {
        Map<String, String> contentDisposition = HttpHeaders.getHeaderOptions(formHeaders, "Content-Disposition");
        name = contentDisposition.get("name");
        filename = contentDisposition.get("filename");
        contentType = formHeaders.get("Content-Type");
//        String contentTransferEncoding = formHeaders.get("Content-Transfer-Encoding");

        this.cachedUpload = File.createTempFile(name, request.getRemoteAddr(), request.getTemporaryStorage());
        OutputStream upload = new BufferedOutputStream(new FileOutputStream(cachedUpload));
        stream.writeFormData(upload, boundary, charset);
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public File getUpload() {
        return cachedUpload;
    }

}
