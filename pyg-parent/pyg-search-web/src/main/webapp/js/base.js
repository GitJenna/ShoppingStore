var app = angular.module('pingyougou', []);

/*$sce服务写成过滤器*/
app.filter('trustHtml',['$sce',function ($sce) {
    return function (data) {
        return $sce.trustAsHtml(data);
    }
}]);