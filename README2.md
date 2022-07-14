발표의 내용과 목적 : 

내용: 클라이언트의 요청이 들어왔을 경우<br>DispatcherServlet이 URL을 분석해서 매핑된 Controller를 찾아줍니다.<br>그 때 도움을 받는 **HandlerMapping**과 **HandlerAdaptor**가 있습니다.<br> 이 때 HandlerAdaptor는 **Controller에게 전달할 Argument**를 어떻게 만드는 역할을 하는데<br> 초점을 두고 설명하겠습니다<br>     - (**ArgumentResolver**와 **MessageConvertor**)

목적 : **ArgumentResolver**의 🔸**MessageConvertor**의 이해와

1. <img width="276" alt="image-20220429152015632" src="https://user-images.githubusercontent.com/58017318/178950130-4831ee1c-0e02-459f-863d-afa77b5c49fe.png">
2. DispatcherServlet은 구현이 되어있고 **어차피 자동으로 되있는거 왜 까보는거지?!**라는 생각을 바꾸고싶다!<br>

-----------

자 ! 그럼 우리가 알고있는 **DispatcherServlet**의 흐름을 한번 봅니다

우리가 자주 본 DispatcherServlet 요청 흐름입니다.

![image-20220427144346940](https://user-images.githubusercontent.com/58017318/178950089-4b68d79e-14dc-4d01-baa6-8f3303877f36.png)

DispatherServlet의 전체 흐름을 설명하면 시간이 너무 오래 걸리니<br>저는 여기서 **HandlerMapping과 HandlerAdaptor** 흐름을 초점을 두고 발표할 예정입니다.

> 참고로 controller handler라고 표현

- **HandlerMapping 역할**

  - 역할 : url에 맞는 handler(Controller)를 찾아 DispatcherServlet에게 넘김

- **HandlerAdaptor 역할**

  - 역할 : HandlerMapping에게 받은 handler(Controller)를 실행해주는 역할

  - **HandlerAdapter** interface를 상속받음

    - ```java
      ModelAndView handle(HttpServletRequest request
                          , HttpServletResponse response, Object handler) throws Exception;
      ```

    - **handle() 메소드는** Controller 실행 전 **알맞은 Argument**를 **Controller**에 전달해주기 위해 존재

      - ArgumentResolver가 존재
        - MessageConvertor

위 내용을 그림으로 살펴보면

![image-20220427140946678](https://user-images.githubusercontent.com/58017318/178950502-c1172d6b-69fd-48bc-9646-86935c9a1a8b.png)



**[테스트 환경 조건]**

```java
// 요청데이터
content-type: application/json
{"username":"hello", "age":20}

// Controller 
@ResponseBody
@RequestMapping("/firstUrl")
public String testUrl(@RequestBody Hello hello) {
	System.out.println("hello = " + hello);
	return hello.toJSON();
}
```

> **@RequestMapping**으로 URL을 매핑한 Controller
>
> - HandlerMapping : RequestMapping**HandlerMapping**
>
> ![image-20220427162311604](https://user-images.githubusercontent.com/58017318/178950097-f51a01ec-aca5-4483-8c5f-ddbbd48546a1.png)
>
> 
>
> - HandlerAdaptor : RequestMapping**HandlerAdapter**
>   - ![image-20220427161936769](https://user-images.githubusercontent.com/58017318/178950095-9e6fc4f2-daf8-4177-9c5a-6b0709660cd3.png)
>   - ArgumentResolver : RequestResponseBodyMethodProcessor
>     - ![image-20220428105731199](https://user-images.githubusercontent.com/58017318/178950100-18d410fb-cad4-4eb9-a149-711fd54e3a31.png)
>     - MessageConvertor : RequestResponseBodyMethodProcessor



## 그럼 여기서 궁금한것!

HandlerMapping을 상속받은 구현체, HandlerAdaptor를 상속받은 구현체는 매우 많습니다

> @RequestMapping으로 선언한 경우
>
> 	- RequestMappingHandlerMapping 구현체
> 	- RequestMappingHandlerAdaptor 구현체

## 그럼 요청이 들어왔을 때 DispatcherServlet은

해당 요청이 **어떤** **HandlerMapping과 HandlerAdaptor**를 어떻게 **가져다 쓰고 사용**하는것일까

-----------------

# 0. Tomcat 을 start할 경우

Tomcat을 start할 경우 <br>HandlerAdapter와 HandlerMapping을 상속 받은 구현체들을 모두 **스프링 컨테이너**에 등록합니다.



## 첫번째,  HandlerAdaptor  등록

저희는 **@RequestMapping으로 URL을 매핑**하기때문에 **RequestMappingHandlerAdapter**라는 구현체를 등록하는 부분을 확인해보겠습니다.

**WebMvcConfigurationSupport.java**

```java
@Bean // [스프링 컨테이너]에 등록
public RequestMappingHandlerAdapter requestMappingHandlerAdapter( ...생략 ... ) {
  RequestMappingHandlerAdapter adapter = createRequestMappingHandlerAdapter();
  
  // RequestMappingHandlerAdapter에 변수들을 셋팅해준다.
    // Hello라는 객체를 Controller에게 만들어서 전달해주는 역할
  adapter.setMessageConverters(getMessageConverters());
  adapter.setCustomArgumentResolvers(getArgumentResolvers());
```

​	

## 	1. adapter.**setMessageConverters**(getMessageConverters());

- - 기능 : **HTTP 요청이나 응답을 메세지로 변환**

    - **StringHttpMessageConverter**이 사용됨

    - 

      ```java
      // Controller 
      @ResponseBody
      @RequestMapping("/firstUrl")
      public String testUrl(@RequestBody Hello hello) {  // MappingJackson2HttpMessageConverter
      	System.out.println("hello = " + hello);
      	return hello.toJSON();
      }
      ```

      - @RequestBody Hello hello 로 Argument를 받아들일 것이기에<br>**MappingJackson2HttpMessageConverter**를 사용

      

  - interface **HttpMessageConverter**

    - 구현체들

    - ```java
      ByteArrayHttpMessageConverter : byte[] 데이터를 처리한다.
      	- 클래스 타입: byte[] , 미디어타입: * / * ,                                           
        - 요청 예) @RequestBody byte[] data
        - 응답 예) @ResponseBody return byte[] 쓰기 미디어타입 application/octet-stream
      
      StringHttpMessageConverter : String 문자로 처리. 클래스 타입: String , 미디어타입: */ *
      	- 요청 예) @RequestBody String data
      	- 응답 예) @ResponseBody return "ok" 쓰기 미디어타입 text/plain
      
      MappingJackson2HttpMessageConverter : application/json
      	- 클래스 타입: 객체 또는 HashMap , 미디어타입 application/json 관련
      	- 요청 예) @RequestBody HelloData data
      	- 응답 예) @ResponseBody return helloData 쓰기 미디어타입 application/json 관련
      ```

​		

## 2. adapter.**setCustomArgumentResolvers**(getArgumentResolvers());

- - 기능 : 

    - 

      ```java
      // request로 받은 데이터를 [원하는 형태의 객체]로 변환할 수 있는지 확인
      boolean supportsParameter(MethodParameter parameter);
      
      // 해당 파라미터를 [원하는 형태의 객체]로 생성하여 return
      Object resolveArgument(...);
      
      /* 
      	- 그리고 이렇게 생성된 객체가 컨트롤러 호출시 넘어가는 것이다.
      	- 이 안에 MessageConverter가 있다.
      */
      ```

  - interface : **HandlerMethodArgumentResolver**

    - 구현체 : 

      - RequestParamMethodArgumentResolver
      - RequestResponseBodyMethodProcessor // 이것이 사용됨

      



## 두번째, HandlerMapping  스프링 컨테이너에 등록

저희는 @RequestMapping으로 URL을 매핑하기때문에 **RequestMappingHandlerMapping**를 등록하는 부분을 확인해보겠습니다.

```java
@Bean
public RequestMappingHandlerMapping requestMappingHandlerMapping( ... 생략 ... ) {
  RequestMappingHandlerMapping mapping = createRequestMappingHandlerMapping();
}
```



---------------

# 1. Client의 첫 요청 시 DispatcherServlet객체를 생성할 수 있도록 도와줍니다.

DispatcherServlet이 뭘까? 

​	- HTTP 모든 요청을 가장 먼저 받아 컨트롤러를 실행해줌

DispatcherServlet은 생성하고 초기화작업을 매번 하면 많은 작업비용이 발생하므로
톰캣 구동 후 첫 요청시에 생성및초기화 하고
두번째요청부터는 생성된 DispatcherServlet을 **재사용**합니다.

 ==> **싱글톤**으로 활용

​	==> 이게 어떤 의미이냐! 첫 요청시에 생성 및 초기화되고,<br>                    두번째 요청부터는 DispatcherServlet을 재사용한다.



![img](https://user-images.githubusercontent.com/58017318/162986428-f654bdbf-4a7c-4881-a853-45cddfa6271c.png)

> DispatcherServlet의 생명주기는 아래 내용 참고
>
> https://victorydntmd.tistory.com/154



Tomcat 구동 후 첫 요청이 들어오면<br>DispatcherServlet 생성 실행 시 initStrategies() 메소드를 사용하여<br>Tomcat구동 시 **스프링컨테이너에 등록**되었던 HandlerMapping, HandlerAdapter들을 DispatcherServlet에 등록한다.

```java
// DispatcherServlet.class
protected void initStrategies(ApplicationContext context) {
  initMultipartResolver(context);
  initLocaleResolver(context);
  initThemeResolver(context);
  initHandlerMappings(context); // handlerMapping
  initHandlerAdapters(context); // handlerAdapter
  initHandlerExceptionResolvers(context);
  initRequestToViewNameTranslator(context);
  initViewResolvers(context);
  initFlashMapManager(context);
}
```



## 1. initHandlerMappings(context);

- ```java
  // DispatcherServlet.class
  private void initHandlerMappings(ApplicationContext context) {
    this.handlerMappings = null;
  
    if (this.detectAllHandlerMappings) {
      // ApplicationContext에서 HandlerMapping.class를 상속받은 클래스들을 찾는다.
      // Tomcat 구동 시 스프링빈에 올라간 HandlerMapping을 모두 찾는다
      Map<String, HandlerMapping> matchingBeans =
        BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
      
      if (!matchingBeans.isEmpty()) {
        // this.handlerMappings 변수에 등록한다.
        this.handlerMappings = new ArrayList<>(matchingBeans.values());
        AnnotationAwareOrderComparator.sort(this.handlerMappings);
      }
    }
  ```

- ![image-20220410210929530](https://user-images.githubusercontent.com/58017318/162986832-d2642cc7-bb84-477d-9d33-f50b6f58ff9c.png)



## 2. initHandlerAdapters(context);

![image-20220410211109824](https://user-images.githubusercontent.com/58017318/162986964-5888da1a-be1a-418b-9a36-fa29cc14095d.png)

----------

# 2. Cilent의 요청을 처리하자<br>Servlet에서 doDispatch() 함수를 통해 원하는 Url의 Controller를 찾아간다.

Servlet의 초기셋팅이 끝나면 요청을 실행하게됩니다.<br>지금부터는 **요청이 들어올 때마다 실행되는 프로세스**입니다.

큰 흐름을 말씀드리면 handler(Controller)를 실행한 후 ArgumentResolver(MessageConverter)를 실행하여
해당 Controller의 Argument를 변환하여 반환해줍니다.

![image-20220427144346940](https://user-images.githubusercontent.com/58017318/178950089-4b68d79e-14dc-4d01-baa6-8f3303877f36.png)

![image-20220427140946678](https://user-images.githubusercontent.com/58017318/178950502-c1172d6b-69fd-48bc-9646-86935c9a1a8b.png)



### 가. DispatcherServlet이 handler, adapter를 찾는다.

```java
protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
  // com.requestbody.requestbody.controller.RequestController#test(Hello)
  mappedHandler = getHandler(processedRequest);
  
  // RequestMappingHandlerAdapter@5407
  HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
  
  // handle을 통해 handler 접근
  mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
```

**@RequestMapping**을 사용하였기에

- handler: **RequestMappingHandlerMapping**를 통해 handler를 찾는다
  - **DispatcherServlet - doDispatch**
  - <img width="967" alt="162987341-5be46663-def6-4f0a-b9ef-6d02fdb30ed4" src="https://user-images.githubusercontent.com/58017318/178951118-bd10aa9b-bfcb-41a3-9263-5f45ff66d91b.png">
- Adapter: **RequestMappingHandlerAdapter**
  - <img width="1071" alt="162987455-e8057656-967c-467d-9523-266d3ad8e641" src="https://user-images.githubusercontent.com/58017318/178951360-daab2c38-0b4b-4717-8d01-3792850c91db.png">



### 나. Adapter.handle메소드를 실행한다.

```java
mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
```

> HandlerAdapter ha.handle를 interface로 선언함으로써
> SOLID원칙
>
> - **OCP: 개방-폐쇄 원칙(Open/closed principle)**
>   - HandlerAdapter interface는 이를 상속받은 Class **확장에는 열려** 있고
>     **변경에는 닫혀** 있어야 한다. ==> **다형성**
>   - interface로 작성하여 구현체를 유연하게 받을 수 있도록 설계된것이다.
> - DIP: 제어의역전 원칙
>   - **변화하지 않는 interface에 의존**하고있다.
>   - ![image-20220328211049852](https://user-images.githubusercontent.com/58017318/162987558-cada7051-765f-4040-9c2b-4eea1c41793a.png)



RequestMappingHandlerAdapter로 가보면 handle메소드를 찾아가보자

```java
@Override
@Nullable
public final ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
  throws Exception {

  return handleInternal(request, response, (HandlerMethod) handler);
}

// handleInternal 타고 
protected ModelAndView handleInternal(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
		mav = this.invokeHandlerMethod(request, response, handlerMethod);
}

// invokeHandlerMethod 타고 들어가보면
```



```java
@Nullable
protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
                                           HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
    if (this.argumentResolvers != null) {
      // invocableMethod에 argumentResolver 셋팅
      invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
    }
    if (this.returnValueHandlers != null) {
      invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
    }
  	
  	//// 생략 /// 
 		invocableMethod.invokeAndHandle(webRequest, mavContainer, new Object[0]);

//  ServletInvocableHandlerMethod.invokeAndHandle
	public void invokeAndHandle( ... 생략 ... ) throws Exception {

// InvocableHandlerMethod.invokeForRequest
    	public Object invokeForRequest( ... 생략 ... ) throws Exception {
		Object[] args = getMethodArgumentValues(request, mavContainer, providedArgs);
            
```



---------

# 3. ArgumentResolver를 통해 Object를 리턴해준다.



HandlerAdaptor를 통해 handle(Controller)을 실행해주는데 실행하기 전, Controller의  Argument를 만드는 작업을 합니다 이때, ArgumentResolver와 **🔸MessageConverter**의 도움을 받습니다

여기서 잠깐!

**ArgumentResolver**란 무엇인가?

- Interface : HandlerMethodArgumentResolver
  - ![image-20220428104756706](https://user-images.githubusercontent.com/58017318/178950098-3e346d1b-af80-425a-9f7c-08ccf3e9c871.png)
- 기능 : Controller에 맞는 Argument Object 리턴을 해준다.
- 메소드 : 

> ```java
> public interface HandlerMethodArgumentResolver {
> // 1. 해당 파라미터를 지원해주는 ArgumentResolver가 있는지 확인  
> boolean supportsParameter(MethodParameter parameter);
> 
> // 2. [1]에서 확인한 ArgumentResolver를 실행하여
> // 해당 Controller에서 원하는 Argument를 Return
> // messageConverter가 존재 
>  // 사용해서 @RequestBody 파라미터를 받을 수 있게 도와줌
> Object resolveArgument( ... 생략 ...) throws Exception;
> }
> ```

- 구현체 : **@RequestBody** -> **RequestResponseBodyMethodProcessor**

  - ```java
    // Controller 
    @ResponseBody
    @RequestMapping("/firstUrl")
    public String testUrl(@RequestBody Hello hello) { //@RequestBody
    	System.out.println("hello = " + hello);
    	return hello.toJSON();
    }
    ```

  - @RequestBody Hello hello

    - 여기서 잠깐! 이름이 **RequestResponseBodyMethodProcessor**

      - ReuqestResponseBody~~ 인거니까 Request뿐만아니라 Response때에도 해당 구현체를 사용하는구나 ㅎㅎ!~

       

그럼 다시 소스로 들어가보면,

**InvocableHandlerMethod.getMethodArgumentValues()**

ArguementResolver를 이용하여 Controller에 맞는 Argument Object 리턴하기

```java
// 만약 Argument가 없다면
if (ObjectUtils.isEmpty(parameters)) {
    return EMPTY_ARGS;
} else {
    // Argument 있다면
    Object[] args = new Object[parameters.length];
    
    /**
    	resolvers들을 모두 조회해서 
    	@RequestBody의 Hello객체로 변환해 줄 수 있는 ArgumentResolver가 존재하는지 확인 
    **/
    if (!this.resolvers.supportsParameter(parameter)) {
        throw new IllegalStateException(formatArgumentError(parameter, "No suitable resolver"));
    }
    
    // Hello객체를 반환해준다
    args[i] = this.resolvers.resolveArgument(parameter, mavContainer, request, this.dataBinderFactory);

    return args;
}
```

-------------

# 4. resolveArgument함수 안에 🔶MessageConvertor를통해 Hello 객체를 반환받자



여기서 잠깐 

ArgumentResolver를 도와주는 **🔶MessageConvertor🔶**에 대해 알아보자

- Interface : HttpMessageConverter

  - ![image-20220428115033139](https://user-images.githubusercontent.com/58017318/178950103-15a1b73f-0102-4018-bff9-978a486ec5f6.png)

- 기능 : 

  > ```
  > argumentResolvers, returnValueHandlers을 도와
  > argument/return 되는 값의 반환을 도화준다.
  > ```

- 메소드 : 

  - ```java
    public interface HttpMessageConverter<T> {
      /**
      Argument의 메시지 컨버터가 
      해당 클래스, 미디어타입을 지원하는지 체크
      **/
      boolean canRead(Class<?> clazz, @Nullable MediaType mediaType);
      
      // 메시지 컨버터를 통해서 메시지를 읽고 Object 전달
      T read(Class<? extends T> class, HttpInputMessage inputMessage) 
        throws IOException, HttpMessageNotReadableException;
      
      boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType);
      void write(T t, @Nullable MediaType contentType, HttpOutputMessage outputMessage)
        throws IOException, HttpMessageNotWritableException;
    }
    ```

- 구현체 :

  - ```java
    // 요청데이터
    content-type: application/json
    {"username":"hello", "age":20}
    
    // Controller 
    @ResponseBody
    @RequestMapping("/firstUrl")
    public String testUrl(@RequestBody Hello hello) {
    	System.out.println("hello = " + hello);
    	return hello.toJSON();
    }
    ```

  - ```java
    /*
    ByteArrayHttpMessageConverter : byte[] 데이터를 처리한다.
    	- 클래스 타입: byte[] , 미디어타입: * / * ,                                           
      - 요청 예) @RequestBody byte[] data
      - 응답 예) @ResponseBody return byte[] 쓰기 미디어타입 application/octet-stream
    
    StringHttpMessageConverter : String 문자로 처리. 클래스 타입: String , 미디어타입: * /*
    	- 요청 예) @RequestBody String data
    	- 응답 예) @ResponseBody return "ok" 쓰기 미디어타입 text/plain
    */
    MappingJackson2HttpMessageConverter : application/json
    	- 클래스 타입: 객체 또는 HashMap , 미디어타입 application/json 관련
    	- 요청 예) @RequestBody HelloData data
    	- 응답 예) @ResponseBody return helloData 쓰기 미디어타입 application/json 관련
    ```

    > @RequestBody Hello 에서 Hello는 byte도 아니고, String도 아니고 객체이므로<br>MappingJackson2HttpMessageConverter를 선택하는데<br>미디어 타입이 **application/json**으로 전송하였으니<br> 지원되는 미디어타입은 \*/\*이므로 해당 Convertor가 선택된다.

  

  그럼 다시 소스로 들어가보면,

  

**AbstractMessageConverterMethodArgumentResolver.readWithMessageConverters**

```java
for (HttpMessageConverter<?> converter : this.messageConverters) {
  // 1. for문을 돌려 알맞은 MessageConverter선택
  GenericHttpMessageConverter<?> genericConverter =
    (converter instanceof GenericHttpMessageConverter ? (GenericHttpMessageConverter<?>) converter : null);
  
  // 2. 선택된 MessageConverter.canRead를 통해 
  // 해당 클래스, 미디어타입을 지원하는지 체크
  if (converter.canRead(targetClass, contentType))) {
    if (message.hasBody()) {
      
      // 3. Argument 데이터를 읽어들인다
      body = converter.read(targetClass, msgToUse);
    }
  }
}
return body; // Hello객체를 반환한다
```



-----------

--------



그렇다면, 

**🔸MessageConverter**가 ArgumentResolver를 도와주어 Controller의 Argument(**Hello**)를 반환해주는건 알겠다!

의문점!

## 어차피 자동 DispatcherServlet이 해주는데  왜 까보는것이냐?!

**🔸MessageConverter**을 알면 아래와 같은 문제를 해결할 수 있습니다.











때는 **2021년10월1일 오후4~5시 다들 퇴근준비를 하던시점에 🔸긴급협조문🔸**을 받게됩니다 

내용 : 베이톡으로 메세지를 전송할 경우, 상대방의 베이톡에서 한글이 깨진다

![image-20220428144538047](https://user-images.githubusercontent.com/58017318/178950108-4d73fdf5-2b7f-4147-a60c-0406224f333e.png)



저희는 방금 **원하는 타입으로 객체를 변환해주는 MessageConver**를 배웠기 때문에<br>위 내용을 토대로 **원인**을 파악하고 **해결**하러 가보겠습니다.



## 1. 원인 파악

문제의 Controller로 가보면 <br>**/ibmessenger/bayTalkByTranSeq**

> Return을 할 경우 데이터가 깨지는 것이기 때문에<br>Return해주는 부분으로 가보면<br>**@ResponseBody**를 사용하여 Return을 **String**으로 해주었습니다.
>
> ![image-20220428145953444](https://user-images.githubusercontent.com/58017318/178950111-756786ce-ea3e-4445-872a-0f5d0d78647d.png)



그럼 아래 그림을 다시 한번 보면

설명드린것은 Controller의 Argument인 Hello라는 객체를 **ArgumentResolver와 MessageConver**를 통해데이터를 만들어줬다.<br>인데 여기서는 String으로 Return해주는 것이므로 ReturnValueHandler가 실행됩니다.<br>이 때, 두객체의 공통점은 Data를 만들 때 **메시지 컨버터**를 사용한다는 것 입니다.

![image-20220428150638995](https://user-images.githubusercontent.com/58017318/178950121-9999e0ae-4fa9-4d72-8676-52d519cf689c.png)

>  ReturnValueHandler의 HTTP 메시지컨버터가 Return을 만들어주는 것임을 알 수 있습니다.

> ![image-20220428145953444](https://user-images.githubusercontent.com/58017318/178950111-756786ce-ea3e-4445-872a-0f5d0d78647d.png)
>
> 그렇다면, @ResponseBody라는 어노테이션을 사용해 **String을 반환**해주는 <Br>MessageConverter는 무엇일까?
>
> ```java
> /*
> ByteArrayHttpMessageConverter : byte[] 데이터를 처리한다.
> 	- 클래스 타입: byte[] , 미디어타입: * /* ,          
>     - 요청 예) @RequestBody byte[] data
>     - 응답 예) @ResponseBody return byte[] 쓰기 미디어타입 application/octet-stream
> 
> MappingJackson2HttpMessageConverter : application/json
> 	- 클래스 타입: 객체 또는 HashMap , 미디어타입 application/json 관련
> 	- 요청 예) @RequestBody HelloData data
> 	- 응답 예) @ResponseBody return helloData 쓰기 미디어타입 application/json 관련
> */
> 
> StringHttpMessageConverter : 
> 	- String 문자로 처리. 클래스 타입: String , 미디어타입: */ *
> 	- 요청 예) @RequestBody String data
> 	- 응답 예) @ResponseBody return "ok" 쓰기 미디어타입 text/plain
> ```



그럼 **StringHttpMessageConverter**을 통해 return을 할 때,<br>**한글이 깨지는 것임을 짐작**할 수 있습니다.



String으로 Return을 하고<br>**StringHttpMessageConverter**와 관련이 깊음을 알 수 있습니다.

![image-20220428150449077](https://user-images.githubusercontent.com/58017318/178950114-0d455e38-1fe4-4889-a58d-235e927ae126.png)



MessageConverter의 interface를 살펴보면

```java
public interface HttpMessageConverter<T> {
  // argument
  boolean canRead(Class<?> clazz, @Nullable MediaType mediaType);
  T read(Class<? extends T> class, HttpInputMessage inputMessage) 
    throws IOException, HttpMessageNotReadableException;
  
   
  // returnValue
  boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType);
  void write(T t, @Nullable MediaType contentType, HttpOutputMessage outputMessage)
    throws IOException, HttpMessageNotWritableException;
}
```



String을 return할 때, 한글이 깨지는 것이므로<br> return해주는 메소드인 **write이니 따라가보면**

판매자 요청 URL에서 **Return**해주는 StringHttp**MessageConverter**의 **write메소드를 살펴보면**<br>미디어타입은 charset이 **utf-8** 붙어있는 반면에

구매자의 미디어타입은 charset이 안붙어있음을 확인할 수 있습니다.

![image-20220428152659536](https://user-images.githubusercontent.com/58017318/178950125-5cf279c6-9b97-48a4-bb23-2962eeae154e.png)

![image-20220428144538047](https://user-images.githubusercontent.com/58017318/178950108-4d73fdf5-2b7f-4147-a60c-0406224f333e.png)





# 그렇다면 문제는 *구매자*에서 <br> *ReturnValue*를 받을 때 *UTF-8이 셋팅이 안되어있어*서 깨지는 것!<br><br>결과적으로<br>[StringHttpMessageConverter]의 <br>ReturnValue에 Charset을 UTF-8로 셋팅해주면<br> 해결되겠죠?!





## 해결방법

```java
public class StringHttpMessageConverter extends AbstractHttpMessageConverter<String> {
    // 생성자를 통해 defaultCharset을 셋팅해주는 부분이 있음을 발견
	public StringHttpMessageConverter(Charset defaultCharset) {
		super(defaultCharset, MediaType.TEXT_PLAIN, MediaType.ALL);
	}
```



web-config.xml에 아래와 같은 내을 넣어주자

```xml
<!-- Configures the @Controller programming model -->
<mvc:annotation-driven>
    <mvc:message-converters>
        <bean class="org.springframework.http.converter.StringHttpMessageConverter">
            <constructor-arg>
                <value>UTF-8</value>
            </constructor-arg>
        </bean>
    </mvc:message-converters>
</mvc:annotation-driven>
```



# 😎해결

![image-20220428153947891](https://user-images.githubusercontent.com/58017318/178950126-cad96621-8bc7-4c64-bb30-3a2483d82cca.png)

> 발표를 마치며,,,,,,







> 소감 및 정리<br>발표를 통해 **MessageConvertor의 도움을 받아 Argument와 return의 데이터를 만들어준다** 정도의 느낌만 가지시면 좋겠고<br> **DispatcherServlet은 구현**이 되어있고 **어차피 자동으로 되는거 왜 까보는거지**라고 생각하기보단<br>까보면서 **크리티컬한 문제를 해결할 수 있구나** 정도의 생각만 가지시면 발표는 성공한듯합니다.
>
> MessageConverter를 함께 파헤쳐주신 **기철과장님**께 감사드리며... 발표는 마무리하도록하겠습니다!

