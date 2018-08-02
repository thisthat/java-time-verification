package server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;
import intermediateModelHelper.indexing.mongoConnector.MongoConnector;
import intermediateModelHelper.indexing.structure.IndexData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.handler.middleware.ParsePars;
import server.handler.middleware.indexMW;
import server.handler.outputFormat.OutputData;
import server.helper.Answer;
import server.indexing.DBDataJSON;
import server.indexing.MongoConnectorServer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class getThreads extends indexMW {

	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void handle(HttpExchange he, Map<String, String> parameters, String name) throws IOException {
		LOGGER.debug("Request getThread on {} parameters: [{}]", name, parameters);
		if(!ParsePars.ParseIndexStatus(name,he)){
			return;
		}
		MongoConnectorServer db = MongoConnectorServer.getInstance(name);
		String base_path = db.getBasePath();
		List<DBDataJSON> threads = db.getClassesWithThread();
		List<OutputData> files = new ArrayList<>();
		for(DBDataJSON d : threads){
			files.add(new OutputData(
					d.getPath().replace(base_path, ""),
					d.getKlassName(),
					d.getPackageName()
			));
		}
		ObjectMapper json = ParsePars.getOutputFormat(parameters);
		json.enable(SerializationFeature.INDENT_OUTPUT);
		String response = json.writeValueAsString(files);
		Answer.SendMessage(response, he);

	}

}
