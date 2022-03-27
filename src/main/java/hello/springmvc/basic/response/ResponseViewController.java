package hello.springmvc.basic.response;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
public class ResponseViewController {
    // ModelAndView로 View 찾기
    @RequestMapping("/response-view-v1")
    public ModelAndView responseView(){
        ModelAndView mav = new ModelAndView("response/hello");
        mav.addObject("data", "hello");

        return mav;
    }

    // String으로도 View 이름 찾기 가능
    @RequestMapping("/response-view-v2")
    public String responseView2(Model model){
        model.addAttribute("data", "hello");
        return "response/hello";
    }

    // @ResponseBody를 붙이면 Http의 응답을
    // response/hello라는 글자로 해주는것!
    @ResponseBody
    @RequestMapping("/response-view-v3")
    public String responseView3(Model model){
        model.addAttribute("data", "hello");
        return "response/hello";
    }

    // void로 하고
    // RequestMapping의 mapping을
    // view 이름과 똑같이 했다면 view를 찾아간다.
    // 비권장!!
    @RequestMapping("/response/hello")
    public void responseView4(Model model){
        model.addAttribute("data", "hello");
    }


}
