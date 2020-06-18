let app = new Vue({
    el: "#app",

    data() {
        return {
            fileList: [],
            searchKeyword: "",
            activeId: null,
            viewImageIndex: 0,
            visible: {
                imageViewer: false,
                background: false
            },
            message: "",
            dev: false,
            test: false,
        }
    },

    methods: {
        /*****  API 기능들 *****/
        // 파일 업로드 
        uploadFileList(files) {
            for (let file of files) {
                let data = {
                    file: file
                };
                axios.post("/upload/v1/file", data, { 
                    headers: { 
                        'Content-Type': 'multipart/form-data;charset=utf-8;' 
                    }
                }).then((result) => {
                    this.insertFileData(result);
                });
            }
        },
        // 파일 변환
        requestConvertFile(data) {
            return new Promise((resolve, reject) => {
                if (this.dev) {
                    setTimeout(() => {
                        this.test = true;
                        resolve();
                    }, 5000);
                } else {
                    axios.post("/convert/v1/images", data).then((result) => {
                        resolve(result);
                    }).catch((error) => {
                        console.log(error)
                    });
                }             
            });
        },
        // 변환 된 파일 목록 불러오기
        getImageList(data) {
            return new Promise((resolve, reject) => {
                if (this.dev) {
                    setTimeout(() => {
                        if (!this.test) {
                            resolve("Image converting is on going");
                            return;
                        }
                        resolve({
                            data: [
                                {
                                    id: "testId",
                                    accessKey: "testKey",
                                    imageName: "document_001.jpg",
                                    extension: "jpg",
                                    length: 0,
                                    url: "https://cdn.dribbble.com/users/194964/screenshots/12110512/media/e1533ce3ef2c0a1a36fb335a0406d141.png"
                                },
                                {
                                    id: "testId2",
                                    accessKey: "testKey2",
                                    imageName: "document_002.jpg",
                                    extension: "jpg",
                                    length: 0,
                                    url: "https://cdn.dribbble.com/users/950937/screenshots/12096088/media/042989fabb3e72347f545d819bdd3a23.png"
                                },
                                {
                                    id: "testId2",
                                    accessKey: "testKey2",
                                    imageName: "document_002.jpg",
                                    extension: "jpg",
                                    length: 0,
                                    url: "https://i.pinimg.com/564x/2f/12/e9/2f12e98153c3c7ad837510d00e4bcfee.jpg"
                                },
                                {
                                    id: "testId2",
                                    accessKey: "testKey2",
                                    imageName: "document_002.jpg",
                                    extension: "jpg",
                                    length: 0,
                                    url: "https://cdn.dribbble.com/users/3466259/screenshots/12099934/media/fd88289a19b5fe1516b7a7e313dde9b5.png"
                                },
                                {
                                    id: "testId2",
                                    accessKey: "testKey2",
                                    imageName: "document_002.jpg",
                                    extension: "jpg",
                                    length: 0,
                                    url: "https://cdn.dribbble.com/users/303292/screenshots/12090810/media/8f0bce5276d9c3706bf9ed43e695732a.png"
                                }
                            ]
                        });
                    }, 250);
                } else {
                    axios.get("/convert/v1/images?accessKey=" + data.accessKey).then((result) => {
                        resolve(result);
                    }).catch((error) => {
                        if (error.response) {
                            console.log(error.response);
                            reject(error.response.data);
                            return;
                        }
                        resolve("console 확인부탁드려요");
                    });;
                }
            });
        },
        /*****  API 기능들 *****/


        /***** Drag & Drop 기능 *****/
        // Drag & Drop 영역 상태 변경 함수 (true => 강조, false => 기본)
        changeDropStatus(type = false) {
            this.visible.background = type;
        },
        // input 으로 file 업로드 시
        changeUploadInput($event) {
            let files = $event.target.files

            this.uploadFileList(files);
            $event.target.value = "";
        },
        // Drag & Drop 영역에 파일 Drop 시
        dropFile($event) {
            let files = $event.dataTransfer.files;

            this.changeDropStatus(false);
            this.uploadFileList(files);
        },
        /***** Drag & Drop 기능 *****/


        /***** 리스트 제어 *****/
        // 업로드 된 파일 정보 리스트에 저장
        insertFileData(fileData) {
            let name = fileData.name;
            let type = "file";
            let lastIndex = fileData.name.lastIndexOf(".");
            
            if (lastIndex != -1) {
                name = fileData.name.substring(0, lastIndex);
                type = fileData.name.substring(lastIndex + 1);
            }

            let id = this.createUniqueId();
            this.fileList.push({
                id: id,
                accessKey: id,
                name: name,
                type: type,
                images: [],
                _size: fileData.size,
                convert: false,
                get size() {
                    let type = ["KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"];
                    let count = 0;
                    let fileSize = this._size / 1024;

                    while (fileSize >= 1024) {
                        fileSize /= 1024;
                        count += 1;
                    }

                    return `${Math.ceil(fileSize * 10) / 10} ${type[count]}`;
                }
            });
        },
        // 검색어로 필터링 된 파일 목록
        getSearchList() {
            return this.fileList.filter(({name, type}) => {
                return `${name}.${type}`.indexOf(this.searchKeyword) != -1;
            });
        },
        selectFile(fileData) {
            this.activeId = fileData.id;
        },
        /***** 리스트 제어 *****/

        /***** 파일 변환 기능 *****/
        convertFile(fileData) {
            fileData.convert = true;
            this.selectFile(fileData);
            
            let data1 = {
                accessKey: fileData.id,
                fileName: `${fileData.name}.${fileData.type}`
            };

            let data2 = {
                accessKey: fileData.accessKey
            };

            this.requestConvertFile(data1).then(() => {
                this.getImageList(data2).then(result => {
                    fileData.images = result.data;
                    this.message = "";
                }, (error) => {
                    this.message = error;
                    alert(error);
                });
            }); 
        },
        getConvertList(fileData) {
            this.message = "";

            let data = {
                accessKey: fileData.accessKey
            };

            this.getImageList(data).then(result => {
                fileData.images = result.data;
                this.message = "";
            }, (error) => {
                this.message = error;
                alert(error);
            });
        },
        /***** 파일 변환 기능 *****/

        /***** 이미지 뷰어 *****/
        openImageViewer(idx = 0) {
            this.viewImageIndex = idx;
            this.visible.imageViewer = true;
        },
        closeImageViwer() {
            this.visible.imageViewer = false;
        },
        prevSlide() {
            this.viewImageIndex = (this.viewImageIndex - 1 + this.activeFile.images.length) % this.activeFile.images.length
        },
        nextSlide() {
            this.viewImageIndex = (this.viewImageIndex + 1 + this.activeFile.images.length) % this.activeFile.images.length
        },
        /***** 이미지 뷰어 *****/

        /***** 공통 함수 *****/
        createUniqueId() {
            let output = "";
            let arr = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890";

            for (let i = 0; i < 10; i++) {
                output += arr[Math.floor(Math.random() * arr.length)];
            }

            return output;
        },
        getImageURL(imageData) {
            if (this.dev) {
                return imageData.url
            } else {
                return '/convert/v1/images/' + imageData.accessKey + '/' + imageData.imageName;
            }
        }
        /***** 공통 함수 *****/
    },

    computed: {
        activeFile() {
            return this.fileList.find(({id}) => id == this.activeId)|| {
                accessKey: "",
                name: "no file selected.",
                type: "Press the Convert button on the file list.",
                size: "",
                images: []
            }
        },
    },

    mounted() {
        // 기본 데이터
        this.insertFileData({name: "doc-large.docx", size: 512 * 1000});
        this.insertFileData({name: "doc-medium.docx", size: 256 * 1000});
        this.insertFileData({name: "doc-small.docx", size: 64 * 1000});
        
        this.insertFileData({name: "hwp-large.hwp", size: 512 * 1000});
        this.insertFileData({name: "hwp-medium.hwp", size: 256 * 1000});
        this.insertFileData({name: "hwp-small.hwp", size: 64 * 1000});

        this.insertFileData({name: "ppt-large.pptx", size: 610 * 1000});
        this.insertFileData({name: "ppt-medium.pptx", size: 421 * 1000});
        this.insertFileData({name: "ppt-small.pptx", size: 355 * 1000});

        this.insertFileData({name: "xlsx-large.xlsx", size: 512 * 1000});
        this.insertFileData({name: "xlsx-medium.xlsx", size: 256 * 1000});
        this.insertFileData({name: "xlsx-small.xlsx", size: 128 * 1000});
    }
});