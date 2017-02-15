package test;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SourceRange;
import ch.uzh.ifi.seal.changedistiller.model.entities.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by giovanni on 14/02/2017.
 */
public class Changes {

    /* example of utilization

    public static void main(String[] args) {
        Changes c = new Changes();
        File oldTmpFile = new File(args[0]);
        File newTmpFile = new File(args[1]);
        FileDistiller fd = ChangeDistiller.createFileDistiller(ChangeDistiller.Language.JAVA);
        fd.extractClassifiedSourceCodeChanges(oldTmpFile, newTmpFile);
        List<SourceCodeChange> sccs = fd.getSourceCodeChanges();
        List<SourceCodeChange> r = c.getData(args[0], args[1], sccs);
    }

    */

    public List<SourceCodeChange> getData(String _old, String _new, List<SourceCodeChange> sccs){
        List<Format> inputData = new ArrayList<>();
        for (SourceCodeChange sourceCodeChange : sccs) {

            if(sourceCodeChange instanceof Update || sourceCodeChange instanceof Move) {
                Format.Type t = Format.Type.EDIT;
                SourceRange range = sourceCodeChange.getChangedEntity().getSourceRange();
                inputData.add(new Format(range.getStart(), range.getEnd()+1, t));
                if(sourceCodeChange instanceof Update){
                    range = ((Update) sourceCodeChange).getNewEntity().getSourceRange();
                    inputData.add(new Format(range.getStart(), range.getEnd()+1, t));
                } else {
                    range = ((Move) sourceCodeChange).getNewEntity().getSourceRange();
                    inputData.add(new Format(range.getStart(), range.getEnd()+1, t));
                }

            } else {
                Format.Type t = Format.Type.INSERT;
                if(sourceCodeChange instanceof Delete) {
                    t = Format.Type.DELETE;
                }
                SourceRange range = sourceCodeChange.getChangedEntity().getSourceRange();
                inputData.add(new Format(range.getStart(), range.getEnd()+1, t));
            }

        }
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(inputData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost("http://127.0.0.1:9000/getFile");
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("data", jsonInString));
            nvps.add(new BasicNameValuePair("old", _old));
            nvps.add(new BasicNameValuePair("new", _new));
            httppost.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse response = httpclient.execute(httppost);
            InputStream stream = response.getEntity().getContent();
            String myString = IOUtils.toString(stream, "UTF-8");
            List<Boolean> retvals = mapper.readValue(myString, List.class);
            List<SourceCodeChange> out = new ArrayList<>();
            System.out.println(myString);
            int i = 0;
            boolean flag = false;
            for (SourceCodeChange sc : sccs) {
                flag = false;
                if(sc instanceof Update || sc instanceof Move) {
                    if(retvals.get(i) || retvals.get(i+1)){
                        out.add(sc);
                        flag = true;
                    }
                    i+=2;
                } else {
                    if(retvals.get(i)){
                        out.add(sc);
                        flag = true;
                    }
                    i++;
                }
                if(!flag && sc.getChangeType().name().equals("SYNCHRONIZED_STATEMENT")){
                    out.add(sc);
                }
            }
            return out;
        }
        catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
