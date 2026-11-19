package com.talk_space;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talk_space.modules.users.api.OnlineCaptchaPhotos;
import org.junit.jupiter.api.Test;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;
class OnlineCaptchaPhotosTest {
    static class Source extends OnlineCaptchaPhotos {
        Source() { super(new ObjectMapper()); }
        @Override protected byte[] get(URI uri,int limit) throws IOException {
            if(uri.getHost().equals("commons.wikimedia.org")) {
                String q=URLDecoder.decode(uri.getRawQuery(),StandardCharsets.UTF_8);
                int category=q.contains("intitle:traffic") ? 0 : q.contains("intitle:car") ? 1 : 2;
                List<Map<String,Object>> pages=new ArrayList<>();
                for(int i=0;i<40;i++) {
                    int id=category*100+i;
                    pages.add(Map.of("pageid",id,"imageinfo",List.of(Map.of("mime","image/png",
                        "thumburl","https://upload.wikimedia.org/"+id+".png",
                        "extmetadata",Map.of("LicenseShortName",Map.of("value","CC0"))))));
                }
                return new ObjectMapper().writeValueAsBytes(Map.of("query",Map.of("pages",pages)));
            }
            int id=Integer.parseInt(uri.getPath().replace("/","").replace(".png",""));
            BufferedImage image=new BufferedImage(2,2,BufferedImage.TYPE_INT_RGB);
            for(int x=0;x<2;x++) for(int y=0;y<2;y++) image.setRGB(x,y,0x110000+id*300);
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); ImageIO.write(image,"png",bytes); return bytes.toByteArray();
        }
    }
    @Test void consecutiveChallengesUseDifferentPhotosAndBalancedCategories() {
        Source source=new Source();
        try {
            var first=source.next(); var second=source.next();
            assertEquals(9,first.size()); assertEquals(9,second.size());
            for(int category=0;category<3;category++) {
                final int c=category;
                assertEquals(3,first.stream().filter(p->p.category()==c).count());
                assertEquals(3,second.stream().filter(p->p.category()==c).count());
            }
            Set<String> data=new HashSet<>(); first.forEach(p->data.add(p.data()));
            assertEquals(9,data.size()); assertTrue(second.stream().noneMatch(p->data.contains(p.data())));
        } finally { source.close(); }
    }
}
