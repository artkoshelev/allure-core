/*global describe:true, it:true, beforeEach:true, afterEach:true, expect:true, spyOn:true, module:true, inject:true, angular:true, jasmine:true */
describe('Testcase controllers', function() {
    'use strict';
    var $controller, $rootScope;

    beforeEach(module('allure.testcase.controllers'));
    beforeEach(inject(function (_$controller_, _$rootScope_) {
        $controller = _$controller_;
        $rootScope = _$rootScope_;
    }));

    describe('StepCtrl', function() {
        function createController(scopeValues) {
            var scope = $rootScope.$new();
            scope = scope.$new();
            angular.extend(scope, scopeValues);
            $controller('StepCtrl', {
                $scope: scope
            });
            return scope;
        }

        it('should detect empty content', function() {
            var scope = createController({
                testcase: {},
                step: {
                    title: 'Dummy step', attachments: [], summary: {steps: 0}
                }
            });
            expect(scope.hasContent).toBe(false);
        });

        it('should detect existing content', function() {
            var scope = createController({
                testcase: {},
                step: {
                    title: 'Dummy step', attachments: [{title: 'file'}], summary: {steps: 0}
                }
            });
            expect(scope.hasContent).toBe(true);
        });
    });

    describe('AttachmentPreviewCtrl', function() {
        var backendDefinitions = {
                'test.txt': 'test content',
                'report.xml': '<root>test content xml</root>',
                'report.json': '{"root": {"content":"test content"}}'
            },
            $httpBackend, stateMock;
        beforeEach(inject(function(_$httpBackend_) {
            $httpBackend = _$httpBackend_;
        }));
        afterEach(function() {
            $httpBackend.resetExpectations();
        });
        function createController(attachment) {
            var scope = $rootScope.$new();
            scope = scope.$new();
            angular.extend(scope, {attachment: attachment});
            $controller('AttachmentPreviewCtrl', {
                $scope: scope,
                $state: stateMock = {
                    go: jasmine.createSpy('gotoStateSpy'),
                    is: jasmine.createSpy('isStateSpy')
                }
            });
            scope.$apply();
            return scope;
        }

        //parametrized test example
        function createDetectImageTest(imageType) {
            it('should detect '+imageType+' type', function() {
                var scope = createController({type: imageType, name: 'picture', source:'pic'});
                expect(scope.type).toBe('image');
            });
        }
        createDetectImageTest('JPG');
        createDetectImageTest('PNG');

        function createDetectCodeTest(language) {
            it('should detect '+language+' code', function() {
                var filename = 'report.'+language.toLowerCase();
                $httpBackend.expectGET('data/'+filename).respond(backendDefinitions[filename]);
                var scope = createController({type: language, name: 'report', source:'report.'+language.toLowerCase()});
                expect(scope.type).toBe('code');

                $httpBackend.flush();
                expect(scope.attachText).toBe(backendDefinitions['report.'+language.toLowerCase()]);
            });
        }

        createDetectCodeTest('XML');
        createDetectCodeTest('JSON');

        it('should detect and work with TXT type', function() {
            var filename = 'test.txt';
            $httpBackend.expectGET('data/'+filename).respond(backendDefinitions[filename]);
            var scope = createController({type: 'TXT', name: 'message', source:'test.txt'});
            expect(scope.type).toBe('text');

            $httpBackend.flush();
            expect(scope.attachText).toBe('test content');
        });

        it('should pass other types', function() {
            var scope = createController({type: 'OTHER', name: 'config', source:'config.properties'});
            expect(scope.type).toBeUndefined();
        });

        it('should re-detect type when attachment has changed', function() {
            $httpBackend.expectGET('data/report.xml').respond(backendDefinitions['report.xml']);
            var scope = createController({type: 'PNG', name: 'picture', source:'pic'});
            expect(scope.type).toBe('image');
            scope.attachment = {type: 'XML', name: 'report', source:'report.xml'};
            scope.$apply();
            expect(scope.type).toBe('code');
        });

        describe('expanded state', function() {
            function createExpanedStateTests(expanded, stateName) {
                it('should detect expanded state ['+expanded+']', function() {
                    var scope = createController({type: 'PNG', name: 'picture', source:'pic'});
                    stateMock.is.andReturn(expanded);
                    expect(scope.isExpanded()).toBe(expanded);
                });
                it('should toggle expanded state ['+expanded+']', function() {
                    var scope = createController({type: 'PNG', name: 'picture', source:'pic'});
                    stateMock.is.andReturn(expanded);
                    scope.toggleExpanded();
                    expect(stateMock.go.calls.length).toBe(1);
                    expect(stateMock.go).toHaveBeenCalledWith(stateName);

                });
            }
            createExpanedStateTests(true, 'home.testsuite.testcase.attachment');
            createExpanedStateTests(false, 'home.testsuite.testcase.attachment.expanded')
        });
    });

    describe('TestcaseCtrl', function() {
        var state, treeUtils,
            scope;
        function createController(testcase) {
            var scope = $rootScope.$new();
            scope = scope.$new();
            $controller('TestcaseCtrl', {
                $scope: scope,
                testcase: testcase,
                $state: state = {
                    current: {data: {baseState: 'base'}},
                    go: jasmine.createSpy('gotoStateSpy'),
                    is: jasmine.createSpy('isStateSpy')
                },
                treeUtils: treeUtils = {walkAround: jasmine.createSpy('treeWalkSpy').andCallFake(function(testcase, prop, callback) {
                    callback(testcase);
                })}
            });
            scope.$apply();
            return scope;
        }
        describe('state checks', function() {
            beforeEach(function() {
                scope = createController({});
            });
            it('should add base when checking isState', function() {
                scope.isState('testcase');
                expect(state.is).toHaveBeenCalledWith('base.testcase');
            });
            it('should go to baseState on closing', function() {
                scope.closeTestcase();
                expect(state.go).toHaveBeenCalledWith('base');
            });
            it('should add base when going', function() {
                scope.go('testcase');
                expect(state.go).toHaveBeenCalledWith('base.testcase');
            });
            it('should go to attachment state', function() {
                scope.setAttachment('log');
                expect(state.go).toHaveBeenCalledWith('base.testcase.attachment', {attachmentUid: 'log'});
            });
        });

        describe('state change handling', function() {
            beforeEach(function() {
                scope = createController({attachments: [{source: 'log', name: 'console log'}]});
            });
            it('should set attachment when uid is present', function() {
                scope.$broadcast('$stateChangeSuccess',  null, {attachmentUid: 'log'});
                expect(treeUtils.walkAround).toHaveBeenCalled();
                expect(scope.attachment).toBeDefined();
            });
            it('should not has attachment on route without uid', function() {
                scope.$broadcast('$stateChangeSuccess',  null, {});
                expect(treeUtils.walkAround).not.toHaveBeenCalled();
                expect(scope.attachment).toBeUndefined();
            });
        });
    });
});
