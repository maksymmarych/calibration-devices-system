angular
    .module('employeeModule')
    .controller('DocumentController', ['$rootScope', '$scope', '$sce', '$http', '$modal', 'DocumentService', 'toaster', '$filter', 'InitializeLibForDigitalSign', function ($rootScope, $scope, $sce, $http, $modal, documentService, toaster, $filter, initializeLibForDigitalSign) {
        $scope.downloadDocument = function (documentType, verificationId, fileFormat) {
            var message = null;
            documentService.isSignedCertificate(verificationId).then(
                function (result) {
                    if (result.data) {
                        documentService.isParsedCertificate(verificationId).then(
                            function (response) {
                                if (!response.data) {

                                    if (!initializeLibForDigitalSign.isInitialized()) {
                                        $http.get('resources/httpproxy.properties').then(function(response){
                                        initializeLibForDigitalSign.initializeWithoutSelectCA(response);
                                        })
                                    }
                                    var fileFormatTemp = 'docx';
                                    documentService.getDocument(documentType, verificationId, fileFormatTemp).then(
                                        function (file) {
                                            var resObj = initializeLibForDigitalSign.getVerifySign(file.data);
                                            message = resObj.notice;
                                            documentService.addSignToDocument(documentType, verificationId, message).then(
                                                function (resp) {
                                                    if (resp) {
                                                        var url = "doc/" + documentType + "/" + verificationId + "/" + fileFormat;
                                                        location.href = url;
                                                        toaster.pop('success', $filter('translate')('INFORMATION'), 'Підпис успішно перевірено!\n' + message);
                                                    }
                                                }
                                            );
                                        }
                                    )
                                } else {
                                    var url = "doc/" + documentType + "/" + verificationId + "/" + fileFormat;
                                    location.href = url;
                                    toaster.pop('success', $filter('translate')('INFORMATION'), 'Підпис успішно перевірено!');
                                }
                            }
                        )
                    } else {
                        toaster.pop('error', $filter('translate')('INFORMATION'), $filter('translate')('ERROR_DOWNLOAD_UNSIGNED_DOCUMENT'));
                    }
                }
            )
        };
        $scope.downloadReport = function (documentType) {
            var url = "doc/report/" + documentType + "/xls";
            location.href = url;
        };
        $scope.printDocument = function (verification) {
            documentService.isSignedCertificate(verification.id).then(
                function (result) {
                    if (result.data) {
                        var documentType = verification.status == 'TEST_OK' ? 'VERIFICATION_CERTIFICATE' : 'UNFITNESS_CERTIFICATE';
                        var url = "doc/" + documentType + "/" + verification.id + "/pdf";
                        var frame = document.getElementById('pdf');
                        var frameDoc = frame.contentDocument || frame.contentWindow.document;
                        frameDoc.documentElement.innerHTML = "";
                        var container = document.getElementById('pdf').contentWindow.document.body;

                        renderPDF(url, container).then(function (success) {
                            console.log(success);
                            setTimeout("document.getElementById('pdf').contentWindow.print()", 500);
                        })
                    } else {
                        toaster.pop('error', $filter('translate')('INFORMATION'), $filter('translate')('ERROR_PRINT_UNSIGNED_DOCUMENT'));
                    }
                }
            )
        };

        function renderPDF(url, canvasContainer) {
            return new Promise(function (resolve, reject) {
                PDFJS.disableWorker = true;
                PDFJS.getDocument(url).then(function (pdfDoc) {
                    renderPages(pdfDoc).then(function (success) {
                        resolve(success);
                    });
                });
            });

            function renderPages(pdfDoc) {
                return new Promise(function (resolve, reject) {
                    for (var num = 1; num <= pdfDoc.numPages; num++)
                        pdfDoc.getPage(num).then(function (page) {
                            renderPage(page);
                            resolve("all pages rendered");
                        });
                });
            }

            function renderPage(page) {
                return new Promise(function (resolve, reject) {
                    var scale = 1;
                    var viewport = page.getViewport(scale);
                    var canvas = document.createElement('canvas');
                    var ctx = canvas.getContext('2d');
                    var renderContext = {
                        canvasContext: ctx,
                        viewport: viewport
                    };

                    canvas.height = viewport.height;
                    canvas.width = viewport.width;
                    canvasContainer.appendChild(canvas);

                    page.render(renderContext);
                    resolve("one page rendered");
                });
            }
        }
    }]);
