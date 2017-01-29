angular
  .module('app')
  .factory('CategoryService', ['$window', '$q', '$http', function($window, $q, $http) {
    function getCategories() {
      return $q(function(resolve, reject) {
        var currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));

        // delete $http.defaults.headers.common['X-Requested-With'];

        console.log('currentUser token:', currentUser.token);
        var req = {
         method: 'GET',
         url: 'http://dreamteamrssfeader.ddns.net:8080/rssserver/categories/',
        //  withCredentials: true,
         headers: {
           'Authorization': currentUser.token,
           'Content-Type': 'application/json'
         }
        }

        $http(req).then(function (res) {
          var categories = res.data;
          resolve(categories);
        }, function(err) {
          console.log('An error has occured while getting categories:', err);
          reject('Server returned an error while getting categories: '+err);
        });
      });
    }

    function addCategory(category) {
      return $q(function(resolve, reject) {
        var currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));

        console.log('currentUser token:', currentUser.token);
        var req = {
         method: 'POST',
         url: 'http://dreamteamrssfeader.ddns.net:8080/rssserver/categories/',
         headers: {
           'Authorization': currentUser.token,
           'Content-Type': 'application/json'
         },
         data: JSON.stringify({
           id_user: currentUser.id_user,
           name: category.name
         })
        }

        $http(req).then(function (res) {
            resolve(res.data);
          }, function(err) {
          console.log('An error has occured while logging in:', err);
          reject('Server returned an error while creating a new category: '+err);
        });
      });
    }

    return {
      getCategories: getCategories,
      addCategory: addCategory
    };
  }]);
