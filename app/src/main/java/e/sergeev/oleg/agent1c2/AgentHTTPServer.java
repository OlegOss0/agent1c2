package e.sergeev.oleg.agent1c2;

import android.content.Context;
import java.util.Map;
import java.util.logging.Logger;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

public class AgentHTTPServer extends NanoHTTPD {
    // TODO: 04.10.2018  

    private static final int PORT   = 24283;
    private static final Logger LOG = Logger.getLogger(AgentHTTPServer.class.getName());
    ExchangeHelper exchangeHelper;
    private Context context;
    private String result = "";

    public AgentHTTPServer(Context mContext) {
        super(PORT);
        context = mContext;
        exchangeHelper = new ExchangeHelper(context);
    }

    public static void main(String[] args) {
        ServerRunner.run(AgentHTTPServer.class);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri    = session.getUri();

        Map<String, String> parms = session.getHeaders();
        String par = parms.get("param");

        result = exchangeHelper.exchange(uri,par);

        String msg = result;

        return newFixedLengthResponse(msg);
    }
}
