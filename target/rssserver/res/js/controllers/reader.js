'use strict';

angular
  .module('app')
  .controller('ReaderController', ['$scope', '$rootScope', '$window', '$state', '$uibModal', 'CategoryService', 'ArticleService', 'FeedService',
      function($scope, $rootScope, $window, $state, $uibModal, CategoryService, ArticleService, FeedService) {

        $scope.currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));

        function getCategories() {
          $scope.defaultCategories = [
            { name: 'Starred items' },
            { name: 'All items' }
          ];
          $rootScope.selectedCategory = $scope.defaultCategories[0];

          CategoryService
            .getCategories()
            .then(function(categories) {
              $scope.categories = categories;
            }, function(err) {
              console.log('Something wrong happened:', err);
            });
        };
        getCategories();

        function getArticles() {
          ArticleService
            .getArticles()
            .then(function(articles) {
              $scope.articles = articles;
            }, function(err) {
              console.log('Something wrong happened:', err);
            });
        };
        getArticles();

        $scope.getFeeds = function(category) {
          FeedService
            .getFeeds(category)
            .then(function(feeds) {
              $scope.feeds = feeds;
            }, function(err) {
              console.log('Something wrong happened:', err)
            });
        }

        $scope.refresh = function() {
          ArticleService
            .getArticles()
            .then(function(articles) {
              $scope.articles = articles;
            }, function(err) {
              console.log('Something wrong happened:', err);
            });
        }

        // Modal's functions
        $scope.openNewCategoryModal = function() {
          var modalInstance = $uibModal.open({
            ariaLabelledBy: 'modal-title-top',
            ariaDescribedBy: 'modal-body-top',
            templateUrl: 'newCategoryModal.html',
            controller: 'ModalInstanceCtrl',
            controllerAs: '$scope',
          });

          modalInstance.result.then(function (newItem) {
            $scope.newItem = newItem;
            CategoryService.addCategory(newItem).then(function(createdItem) {
              // Category created
              console.log('New category:', createdItem);
              Category.getCategories().then(function(categories) {
                $scope.categories = categories;
              }, function(err) {
                console.log('An error has occcured while getting categories:', err);
              });
            }, function(err) {
              console.log('An error has occcured while creating a new category:', err);
            });
          }, function () {
            console.info('Modal dismissed at: ' + new Date());
          });
        };

        $scope.openNewFeedModal = function() {
          var modalInstance = $uibModal.open({
            ariaLabelledBy: 'modal-title-top',
            ariaDescribedBy: 'modal-body-top',
            templateUrl: 'newFeedModal.html',
            controller: 'ModalInstanceCtrl',
            controllerAs: '$scope'
          });

          modalInstance.result.then(function (newItem) {
            $scope.newItem = newItem;
            FeedService.addFeed(newItem).then(function(createdItem) {
              // Feed created

            }, function(err) {
              console.log('An error has occcured while creating a new feed:', err);
            });
          }, function () {
            console.info('Modal dismissed at: ' + new Date());
          });

        };


        $scope.toggleStarredArticle = function(article) {
          article.starred = !article.starred;
        };

  }])
 .controller('ModalInstanceCtrl', function($uibModalInstance) {
   var $scope = this;

   $scope.cancelModal = function() {
     $uibModalInstance.dismiss('cancel');
   };

  $scope.createItem = function (newItem) {
    $uibModalInstance.close(newItem);
  };

 })
 .directive('myLabel', ['$rootScope', function($rootScope) {
   return {
     restrict: 'A',
     scope: {
       category: '='
     },
     link: function(scope, element, attrs) {
         element.on('click', function() {
           element.parent().parent().find('li.active').removeClass('active');
           element.parent().addClass('active');
           $rootScope.selectedCategory = scope.category;
           $rootScope.selectedFeed = {};
           scope.$apply();
         });
       }
   }
}])
.directive('myFeedLink', ['$rootScope', function($rootScope) {
  return {
    restrict: 'A',
    scope: {
      feed: '='
    },
    link: function(scope, element, attrs) {
        element.on('click', function() {
          element.parent().find('a.feed-link.active').removeClass('active');
          element.addClass('active');
          $rootScope.selectedFeed = scope.feed;
          scope.$apply();
        });
      }
  }
}]);
