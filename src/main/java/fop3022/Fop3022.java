package fop3022;

import static utils.Helper.getPath;
import static utils.Helper.getURI;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.xmlgraphics.image.loader.ImageSource;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageSessionContext.UnrestrictedFallbackResolver;
import org.apache.xmlgraphics.image.loader.impl.PreloaderRawPNG;
import org.apache.xmlgraphics.image.loader.impl.imageio.PreloaderImageIO;
import org.apache.xmlgraphics.util.MimeConstants;

/**
 * https://issues.apache.org/jira/browse/FOP-3022
 * 
 * Applies to Fop 2.7
 * 
 * This sample code demonstrates an issue with external graphics as PNG cannot
 * be loaded though that shouldn't be the case.
 * Here is what happens:
 * 
 * Setting a breakpoint in {@link FOUserAgent#resolveURI(String)} will show that 
 * the URI itself is correctly passed as an argument.
 * However the created {@link StreamSource} will get the BaseUri as it's system ID. 
 * This is already wrong as different URIs will share the same system ID here.
 * The system ID will point to the fop configuration file.
 * 
 * Setting a breakpoint into {@link UnrestrictedFallbackResolver#createSource(javax.xml.transform.Source, String)} 
 * will show how the effect of the error.
 * This method fetches the URL from the system ID which points to the fop configuration 
 * file and not to the image.
 * Since this file is accessible through the filesystem the {@link ImageSource} will 
 * be created using the fop configuration file instead of the image.
 *
 * This means that classes such as {@link PreloaderRawPNG} and {@link PreloaderImageIO} 
 * will actually load an XML file (fop configuration) instead of the actual image.
 */
public class Fop3022 {

  private static FopFactory newFactory() {
    try {
      return FopFactory.newInstance(getURI("fop3022/fop-config.xml"));
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }
  
  private static Fop newFop(FopFactory fopFactory, OutputStream outstream) {
    try {
      return fopFactory.newFop(MimeConstants.MIME_PDF, fopFactory.newFOUserAgent(), outstream);
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }
  
  public static void main(String[] args) throws Exception {
  
    // load the FOP content and substitute the file property by the URL
    var pngFile            = getPath("fop3022/sample_640x426.png");
    var inputXml           = Files.readString(getPath("fop3022/input.xml"), StandardCharsets.UTF_8)
      .replaceAll(Pattern.quote("${file}"), pngFile.toUri().toString());
    
    // print out the FOP content to verify that the file property is properly substituted
    // System.err.printf("FOP:\n%s\n\n", inputXml); 

    // standard fop/xslt handling
    var byteout     = new ByteArrayOutputStream();
    var fop         = newFop(newFactory(), byteout);
    var src         = new StreamSource(new StringReader(inputXml));
    var res         = new SAXResult(fop.getDefaultHandler());
    var transformer = TransformerFactory.newInstance().newTransformer();
    transformer.transform(src, res);
    
    Files.write(Paths.get("./out.pdf"), byteout.toByteArray());
          
  }
  
} /* ENDCLASS */
