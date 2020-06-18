# Simple Project using ProcessBuilder

ProcessBuilder 를 이용하여 docker에 특정 명령(이미지변환)을 보내고, 결과값을 mongo db에 저장하는 시나리오

## REST API (http://localhost:8080/swagger-ui.html)
* 이미지 변환 요청
    * POST http://localhost:8080/convert/v1/images
    * body 예제 { accessKey: xxx, filename: sample.hwp } 
    
* 이미지 열람 요청
    * GET http://localhost:8080/convert/v1/images?accessKey=xxx
    * param 으로 accessKey를 이용하며 변환된 이미지 리스트 응
    
* 이미지 다운로드
    * GET http://localhost:8080/convert/v1/images/{accessKey}/{imageName}

## CentOs7 도커 관련
* Docker 파일생성 (나중에 정리하자)
    ```yaml
    FROM docker.io/centos:7
    
    # 사용자 지정
    USER root
    
    # 언어셋 설치
    RUN yum clean all \
     && yum repolist \
     && yum -y update \
     && sed -i "s/en_US/all/" /etc/yum.conf  \
     && yum -y reinstall glibc-common
    
    # 기본적으로 필요한 OS 패키지를 설치한다.
    RUN  yum -y install tar unzip vi vim telnet net-tools curl openssl \
     && yum -y install apr apr-util apr-devel apr-util-devel \
     && yum -y install elinks locate  \
     && yum clean all
    
    ENV LANG=ko_KR.utf8 TZ=Asia/Seoul
    
    # 컨테이너 실행시 실행될 명령
    CMD ["/bin/bash"]
    ```
* 도커 실행 - 볼륨연결
    * docker run -it -v /Users/Downloads/doc-conv-test/output:/doc-conv/output -d centos7-base
* 도커 내부에서 jdk 설치
    * docker exec -it {containerName} /bin/bash
    * yum install java1.8~~
    * which javac
    * readlink -f /usr/bin/javac
    * vim /etc/profile 에 JAVA_HOME 으로 export 추가
    * source /etc/profile
* 도커 외부에서 파일 전달
    * docker cp {외부파일} {containerName:내부경로}
* 도커 외부에서 명령어 실행
    * docker exec {containerName} sh -c "파라멘터 포함 명령어"
    * docker exec {containerName} sh -c "./doc-conv/sdk/convert.sh /doc-conv/input/sample1.pptx /doc-conv/output/ image"    
