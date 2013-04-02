/**
 * Your application code goes here
 */

package userclasses;

import com.codename1.components.InfiniteProgress;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.NetworkEvent;
import com.codename1.io.NetworkManager;
import com.codename1.io.services.ImageDownloadService;
import generated.StateMachineBase;
import com.codename1.ui.*; 
import com.codename1.ui.events.*;
import com.codename1.ui.util.Resources;
import com.codename1.xml.Element;
import com.codename1.xml.XMLParser;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 *
 * @author BCirot
 * FlickrAPI (Search method) : http://www.flickr.com/services/api/flickr.photos.search.html
 */
public class StateMachine extends StateMachineBase {
    
    public final static String flickrURL = "http://api.flickr.com/services/rest/";
    
    public final static String apiKeyArg = "api_key";
    public final static String apiKeyVal = "API_KEY_HERE";
    
    public final static String methodArg = "method";
    public final static String methodVal = "flickr.photos.search";
    
    public final static String searchArg = "text";
    public final static String resultNumberArg = "per_page";
    public final static String resultNumberVal = "10";
    
    public StateMachine(String resFile) {
        super(resFile);
        // do not modify, write code in initVars and initialize class members there,
        // the constructor might be invoked too late due to race conditions that might occur
    }
    
    /**
     * this method should be used to initialize variables instead of
     * the constructor/class scope to avoid race conditions
     */
    protected void initVars(Resources res) {
    }
    
    private ConnectionRequest request;
    private Form mainForm;

    @Override
    protected void onMain_ButtonAction(Component c, ActionEvent event) {

        mainForm = Display.getInstance().getCurrent();
        
        request = new ConnectionRequest();
        
        // Show loading
        InfiniteProgress ip = new InfiniteProgress();
        Dialog dlg = ip.showInifiniteBlocking();
        request.setDisposeOnCompletion(dlg);
        
        // Call arguments
        request.setUrl(flickrURL);
        request.setPost(false);
        request.addArgument(resultNumberArg, resultNumberVal);
        request.addArgument(methodArg, methodVal);
        request.addArgument(apiKeyArg, apiKeyVal);
        request.addArgument(searchArg, findSearchInput().getText());
        
        request.addResponseListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                NetworkEvent netEvt = (NetworkEvent) evt;
                byte[] dataArray = (byte[]) netEvt.getMetaData();
                
                // Results parsing
                XMLParser parser = new XMLParser();
                InputStream is = new ByteArrayInputStream(dataArray);
                Element xmlElement = parser.parse(new InputStreamReader(is));
                Element xmlPhotoElement = (Element) xmlElement.getChildrenByTagName("photos").get(0);
                Vector<Element> photos = xmlPhotoElement.getChildrenByTagName("photo");
                
                // Remove current image list
                Container imageCont = findImagesContainer(mainForm);
                if (imageCont.getComponentCount() != 0) {
                    imageCont.removeAll();
                }
                mainForm.repaint();
                
                for (Element photo : photos) {
                    String id = photo.getAttribute("id");
                    String secret = photo.getAttribute("secret");
                    String farm = photo.getAttribute("farm");
                    String server = photo.getAttribute("server");
                    Label label = new Label();
                    ImageDownloadService.createImageToStorage("http://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + secret + ".jpg", label, id, null);
                    imageCont.addComponent(label);
                }
            }
        });
        
        NetworkManager.getInstance().addToQueue(request);
    }
}
