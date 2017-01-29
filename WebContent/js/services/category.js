angular
  .module('app')
  .factory('CategoryService', ['$window', '$q', '$http', function($window, $q, $http) {
    function getCategories() {
      return $q(function(resolve, reject) {
        var currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));

        var req = {
         method: 'GET',
         url: 'http://dreamteamrssfeader.ddns.net:8080/rssserver/api/categories/',
         headers: {
           'Authorization': currentUser.token,
           'Content-Type': 'application/json'
         }
       };

        $http(req).then(function (res) {
          var categories = res.data.data;
          resolve(categories);
        }, function(err) {
          console.log('An error has occured while getting categories:', err);
          reject('Server returned an error while getting categories: '+err);
        });
      });
    }

    function getCategory(categoryId) {
      return $q(function(resolve, reject) {
        var currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));

        var req = {
         method: 'GET',
         url: 'http://dreamteamrssfeader.ddns.net:8080/rssserver/api/categories/'+categoryId,
         headers: {
           'Authorization': currentUser.token,
           'Content-Type': 'application/json'
         }
       };

        $http(req).then(function (res) {
          var category = res.data.data[0];
          resolve(category);
        }, function(err) {
          console.log('An error has occured while getting category', categoryId, ':', err);
          reject('Server returned an error while getting category'+categoryId+': '+err);
        });


      });
    }

    function addCategory(category) {
      return $q(function(resolve, reject) {
        var currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));

        var req = {
         method: 'POST',
         url: 'http://dreamteamrssfeader.ddns.net:8080/rssserver/api/categories/',
         headers: {
           'Authorization': currentUser.token,
           'Content-Type': 'application/json'
         },
         data: JSON.stringify({
           name: category.name
         })
       };

        $http(req).then(function (res) {
            resolve(res.data);
          }, function(err) {
          console.log('An error has occured while logging in:', err);
          reject('Server returned an error while creating a new category: '+err);
        });
      });
    }

    function deleteCategory(category) {
      return $q(function(resolve, reject) {
        var currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));

        var req = {
         method: 'DELETE',
         url: 'http://dreamteamrssfeader.ddns.net:8080/rssserver/api/categories/'+category.id_cat,
         headers: {
           'Authorization': currentUser.token
         }
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
      getCategory: getCategory,
      addCategory: addCategory,
      deleteCategory: deleteCategory
    };
  }]);
