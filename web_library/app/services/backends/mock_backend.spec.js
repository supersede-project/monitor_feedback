define(["require", "exports", './mock_backend', './../mocks/mocks_loader'], function (require, exports, mock_backend_1, mocks_loader_1) {
    "use strict";
    describe('Mock Backend', function () {
        var configurationMockBackend;
        var feedbackMockBackend;
        beforeEach(function () {
            var configurationMockData = mocks_loader_1.readJSON('app/services/mocks/configurations_mock.json', '/base/');
            configurationMockBackend = new mock_backend_1.MockBackend(configurationMockData);
            var feedbackMockData = mocks_loader_1.readJSON('app/services/mocks/feedbacks_mock.json', '/base/');
            feedbackMockBackend = new mock_backend_1.MockBackend(feedbackMockData);
        });
        it('should list all the mock data', function () {
            configurationMockBackend.list(function (data) {
                expect(data.length).toBe(2);
            });
        });
        it('should retrieve a single object in the mock data', function () {
            var expectedObject = {
                "id": 2,
                "general_configurations": [
                    {
                        "id": 1,
                        "key": "review",
                        "value": true
                    }
                ],
                "pull_configurations": [],
                "mechanisms": []
            };
            configurationMockBackend.retrieve(2, function (object) {
                expect(object).toEqual(expectedObject);
            });
        });
        it('should create a new object in the mock data', function () {
            feedbackMockBackend.list(function (data) {
                expect(data.length).toBe(1);
            });
            var newFeedback = {
                "title": "Feedback 2",
                "configVersion": null,
                "text": "This is the feedback text",
                "user_identification": "u8102390",
                "language": "DE",
                "context_information_id": null
            };
            feedbackMockBackend.create(newFeedback);
            feedbackMockBackend.list(function (data) {
                expect(data.length).toBe(2);
            });
        });
        it('should update an object in the mock data', function () {
            var attributes = {
                "title": "New title for this feedback object"
            };
            feedbackMockBackend.update(1, attributes);
            feedbackMockBackend.retrieve(1, function (updatedFeedback) {
                expect(updatedFeedback.title).toEqual('New title for this feedback object');
            });
        });
        it('should delete an object in the mock data', function () {
            feedbackMockBackend.list(function (data) {
                expect(data.length).toBe(1);
            });
            feedbackMockBackend.destroy(1);
            feedbackMockBackend.list(function (data) {
                expect(data.length).toBe(0);
            });
        });
    });
});
//# sourceMappingURL=mock_backend.spec.js.map