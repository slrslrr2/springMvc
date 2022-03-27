package hello.springmvc.basic.response;

import hello.springmvc.basic.HelloData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
//@Controller
@RestController// 는 @ResponseBody + @Controller를 합친 것
                // @RestController를 적용하면 아래 메소드가 모두 적용된다
public class ResponseBodyController {

    @RequestMapping("/response-body-v1")
    public void ResponseBodyV1(HttpServletResponse response) throws IOException {
        response.getWriter().write("ok");
    }

    @RequestMapping("/response-body-v2")
    public ResponseEntity<String> ResponseBodyV2(){
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping("/response-body-v3")
    public ResponseEntity<HelloData> ResponseBodyV3(HttpEntity<HelloData> httpEntity){
        HelloData helloData = (HelloData) httpEntity.getBody();
        return new ResponseEntity<>(helloData, HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @RequestMapping("/response-body-v4")
    public String ResponseBodyV4(){
        return "ok";
    }

    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @RequestMapping("/response-body-v5")
    public HelloData ResponseBodyV5(HttpEntity<HelloData> httpEntity){
        HelloData helloData = (HelloData) httpEntity.getBody();
        return helloData;
    }
}
