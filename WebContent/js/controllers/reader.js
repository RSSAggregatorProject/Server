'use strict';

angular
  .module('app')
  .controller('ReaderController', ['$scope', '$rootScope', '$window', '$state', '$uibModal', 'CategoryService', 'ArticleService', 'FeedService',
      function($scope, $rootScope, $window, $state, $uibModal, CategoryService, ArticleService, FeedService) {

        $scope.currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));
        $scope.errorAlert = {
          msg: "Something unexpected happened, please try again!",
          active: false
        };

        // Alert's functions
        $scope.addAlert = function() {
          $scope.errorAlert.active = true;
        };

        $scope.closeAlert = function() {
          $scope.errorAlert.active = false;
        };
        // End Alert's functions


        // Categories' functions
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
              $scope.nbAllArticles = 0;
              $scope.getStarredItems();
              categories.forEach(function(category) {
                $scope.nbAllArticles += category.unread;
              });
            }, function(err) {
              $scope.addAlert();
              console.log('Something wrong happened:', err);
            });
        };
        getCategories();

        $scope.getCategory = function(categoryId, index) {
          if (index >= 0) {
            $scope.feeds = $scope.categories[index].feeds;
          }
          CategoryService.getCategory(categoryId).then(function(category) {
            $scope.articles = [];
            category.feeds.forEach(function(feed) {
              $scope.articles = $scope.articles.concat(feed.items);
            });
          }, function(err) {
            $scope.addAlert();
            console.log('Something wrong happened:', err);
          });
        };


        $scope.deleteCategory = function(category) {
          CategoryService
            .deleteCategory(category)
            .then(function(res) {
              console.log('Category deleted:', category);
            }, function(err) {
              $scope.addAlert();
              console.log('An error has occured while deleting the category', category,':', err);
            });
        };
        // End Categories' functions


        // Feeds' functions
        $rootScope.getFeed = function() {
          if ($rootScope.selectedFeed) {
            FeedService
              .getFeed($rootScope.selectedFeed)
              .then(function(feed) {
                $scope.articles = feed.items;
              }, function(err) {
                $scope.addAlert();
                console.log('Something wrong happened:', err);
              });
          }
        };
        $scope.unsubscribeFeed = function(feed) {
          FeedService
            .deleteFeed(feed)
            .then(function(res) {
              console.log('Feed deleted:', feed);
              $rootScope.selectedFeed = {};
              getCategories();
            }, function(err) {
              $scope.addAlert();
              console.log('An error has occured while deleting the category', feed,':', err);
            });
        };
        // End Feeds' functions


        // Items' functions
        $scope.getAllItems = function() {
          FeedService
            .getFeeds()
            .then(function(feeds) {
              if (feeds) {
                $scope.articles = [];
                $scope.nbAllArticles = 0;
                feeds.forEach(function(feed) {
                  $scope.articles = $scope.articles.concat(feed.items);
                  $scope.nbAllArticles += feed.unread;
                });
              } else {
                $scope.articles = [];
              }
            }, function(err) {
              $scope.addAlert();
              console.log('Something wrong happened:', err);
            });
        };

        $scope.getStarredItems = function() {
          FeedService
            .getFeeds(true)
            .then(function(feeds) {
              if (feeds) {
                $scope.articles = [];
                $scope.nbStarredArticles = 0;
                feeds.forEach(function(feed) {
                  $scope.articles = $scope.articles.concat(feed.items);
                  $scope.nbStarredArticles += feed.unread;
                });
              } else {
                $scope.articles = [];
              }
            }, function(err) {
              $scope.addAlert();
              console.log('Something wrong happened:', err);
            });
        };
        // End Items' functions

        $scope.refresh = function() {
          if ($rootScope.selectedFeed.id_feed) {
            $rootScope.getFeed();
          } else if ($rootScope.selectedCategory) {
            console.log('Refresh Selected category:', $rootScope.selectedCategory);
            if ($rootScope.selectedCategory.index == -1) {
              $scope.getStarredItems();
            } else if ($rootScope.selectedCategory.index == -2) {
              $scope.getAllItems();
            } else {
              $scope.getCategory($rootScope.selectedCategory.id_cat, $rootScope.selectedCategory.index);
            }
          }

        };


        // Modal's functions
        $scope.openNewCategoryModal = function() {
          var modalInstance = $uibModal.open({
            ariaLabelledBy: 'modal-title-top',
            ariaDescribedBy: 'modal-body-top',
            templateUrl: 'newCategoryModal.html',
            controller: 'ModalInstanceCtrl',
            controllerAs: '$scope',
            resolve: {
              categories: function () {
                return $scope.categories;
              }
            }
          });

          modalInstance.result.then(function (newItem) {
            $scope.newItem = newItem;
            CategoryService.addCategory(newItem).then(function(createdItem) {
              // Category created
              console.log('New category:', createdItem);
              getCategories();
            }, function(err) {
              $scope.addAlert();
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
            controllerAs: '$scope',
            resolve: {
              categories: function () {
                return $scope.categories;
              }
            }
          });

          modalInstance.result.then(function (newItem) {
            $scope.newItem = newItem;
            FeedService.addFeed(newItem).then(function(createdItem) {
              console.log('New feed:', createdItem);
              getCategories();

            }, function(err) {
              $scope.addAlert();
              console.log('An error has occcured while creating a new feed:', err);
            });
          }, function () {
            console.info('Modal dismissed at: ' + new Date());
          });

        };
        // End Modal's functions


        // Starred/Read handlers
        $scope.toggleStarredArticle = function(article) {
          article.starred = !article.starred;
          if (article.starred) {
            ArticleService
              .starArticle(article)
              .then(function(updatedItem) {
                console.log('Article', article, 'correctly starred');
                if (!article.read) {
                  $scope.nbStarredArticles += 1;
                }
              }, function(err) {
                console.log('An error has occcured while updating article', article,':', err);
              });
          } else {
            ArticleService
              .unstarArticle(article)
              .then(function(updatedItem) {
                console.log('Article', article, 'correctly unstarred');
                if (!article.read) {
                  $scope.nbStarredArticles -= 1;
                }
                if ($rootScope.selectedCategory == $scope.defaultCategories[0]) {
                  $scope.getStarredItems();
                }
              }, function(err) {
                console.log('An error has occcured while updating article', article,':', err);
              });
          }
        };

        $scope.read = function(article) {
          article.open = !article.open;
          if (!article.read) {
            article.read = true;
            if ($rootScope.selectedCategory.index) {
              $scope.categories[$rootScope.selectedCategory.index].unread -= 1;
            }
            ArticleService
              .readArticle(article)
              .then(function(updatedItem) {
                console.log('Article', article, 'correctly read');
              }, function(err) {
                console.log('An error has occcured while updating article', article,':', err);
              });
          }
        };
        // End Starred/Read handlers

  }])
 .controller('ModalInstanceCtrl', function($uibModalInstance, categories) {
   var $scope = this;
   $scope.categories = categories;

   $scope.cancelModal = function() {
     $uibModalInstance.dismiss('cancel');
   };

  $scope.createItem = function (newItem) {
    console.log('New item before:', newItem);
    $uibModalInstance.close(newItem);
  };

 })
 .directive('myLabel', ['$rootScope', function($rootScope) {
   return {
     restrict: 'A',
     scope: {
       category: '=',
       index: '@'
     },
     link: function(scope, element, attrs) {
         element.on('click', function() {
           element.parent().parent().find('li.active').removeClass('active');
           element.parent().addClass('active');
           $rootScope.selectedCategory = scope.category;
           $rootScope.selectedCategory.index = scope.index;
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
          $rootScope.getFeed();
          scope.$apply();
        });
      }
  }
}]);
