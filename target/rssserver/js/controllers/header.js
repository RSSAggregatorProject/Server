'use strict';

angular
  .module('app')
  .controller('HeaderController', ['$scope', '$rootScope', '$window', '$state',
      function($scope, $rootScope, $window, $state) {

        $scope.currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));

        $scope.logout = function() {
          $window.sessionStorage.setItem("currentUser", null);
          $state.go('login');
        };


}]);
