angular
    .module('employeeModule')
    .factory('VerificationPlanningTaskService', ['$http', function ($http) {

        return {
           saveTask: function (task) {
                return save('/task/save', task);
            },
           saveTaskForTeam: function (task) {
                return save('task/team/save', task);
           },
           getVerificationsByCalibratorEmployeeAndTaskStatus: function (pageNumber, itemsPerPage, search, sortCriteria, sortOrder) {
                return getDataWithParams('task/findAll/' + pageNumber + '/' + itemsPerPage + '/' + sortCriteria + '/' + sortOrder, search);
           },
           getModules: function (place, pickerDate, applicationFiled) {
                return getData('task/findAllModules/' + place + '/' + pickerDate + '/' + applicationFiled);
           },
           getTeams: function (pickerDate, applicationFiled) {
                return getAvailableTeams('task/findAllTeams/'  + pickerDate + '/' + applicationFiled);
           },
           getSymbolsAndStandartSizes: function (verificationId) {
                return getData('task/findSymbolsAndSizes/' + verificationId);
           },
            getEarliestPlanningTaskDate: function() {
                return getData('task/earliest_date');
            }
        };

        function save (url, task) {
            return $http.post(url, task)
                .success(function (data) {
                    return data;
                }).error(function (err) {
                    console.log(err);
                    return err;
                });
        }

        function getData (url) {
            return $http.get(url)
            .success(function (data) {
                return data;
            }).error(function (err) {
                return err;
            });
        }

        function getDataWithParams(url, params) {
            return $http.get(url, {
                params: params
            }).success(function (data) {
                return data;
            }).error(function (err) {
                return err;
            });
        }

        function getAvailableTeams (url) {
            return $http.get(url)
                .success(function (data) {
                    return data;
                }).error(function (err) {
                    return err;
                });
        }

    }]);
