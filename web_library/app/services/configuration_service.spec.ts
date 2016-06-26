import {ConfigurationService} from './configuration_service';
import {textType} from '../models/mechanism';
import {ParameterPropertyPair} from '../models/parameter_property_pair';


describe('Mechanism Service', () => {
    let configurationService:ConfigurationService;

    beforeEach(() => {
        var data = [
            {
                "type": "TEXT_TYPE",
                "active": true,
                "order": 1,
                "canBeActivated": false,
                "parameters": [
                    {
                        "key": "maxLength",
                        "value": 100
                    },
                    {
                        "key": "title",
                        "value": "Feedback"
                    },
                    {
                        "key": "hint",
                        "value": "Enter your feedback"
                    },
                    {
                        "key": "textareaFontColor",
                        "value": "#000000"
                    },
                    {
                        "key": "fieldFontType",
                        "value": "italic"
                    },
                    {
                        "key": "maxLengthVisible",
                        "value": 1
                    },
                    {
                        "key": "labelPositioning",
                        "value": "left"
                    },
                    {
                        "key": "labelColor",
                        "value": "#00ff00"
                    },
                    {
                        "key": "labelFontSize",
                        "value": 13
                    },
                ]
            },
            {
                "type": "AUDIO_TYPE",
                "active": false,
                "order": 2,
                "canBeActivated": false,
                "parameters": [
                    {
                        "key": "maxTime",
                        "value": 30,
                        "defaultValue": 30
                    }
                ]
            },
            {
                "type": "SCREENSHOT_TYPE",
                "active": false,
                "order": 3,
                "canBeActivated": false,
                "parameters": [
                    {
                        "key": "title",
                        "value": "Title for screenshot feedback"
                    },
                    {
                        "key": "defaultPicture",
                        "value": "lastScreenshot",
                        "defaultValue": "noImage",
                        "editableByUser": true
                    }
                ]
            },
            {
                "type": "RATING_TYPE",
                "active": true,
                "order": 4,
                "canBeActivated": true,
                "parameters": [
                    {
                        "key": "title",
                        "value": "Rate your user experience"
                    },
                    {
                        "key": "ratingIcon",
                        "value": "star"
                    },
                    {
                        "key": "maxRating",
                        "value": 5
                    },
                    {
                        "key": "defaultRating",
                        "value": 2,
                        "defaultValue": 0,
                        "editableByUser": false
                    }
                ]
            }
        ];
        configurationService = new ConfigurationService(data);
    });

    it('should return a configuration object with all the configuration', () => {
        var configuration = configurationService.getConfig();
        expect(configuration.length).toBe(4);

        var textMechanismConfig = configuration[0];
        expect(textMechanismConfig.type).toEqual('TEXT_TYPE');
        expect(textMechanismConfig.active).toEqual(true);
        expect(textMechanismConfig.order).toEqual(1);
        expect(textMechanismConfig.canBeActivated).toEqual(false);

        var ratingMechanismConfig = configuration[3];
        expect(ratingMechanismConfig.type).toEqual('RATING_TYPE');
    });

    it('should return the corresponding mechanisms', () => {
        var textMechanism = configurationService.getMechanismConfig('TEXT_TYPE');

        expect(textMechanism).not.toBeNull();

        expect(textMechanism.type).toEqual('TEXT_TYPE');
        expect(textMechanism.active).toEqual(true);
        expect(textMechanism.order).toEqual(1);
        expect(textMechanism.canBeActivated).toEqual(false);
    });

    it('should return the context for the view with the configuration data', () => {
        var context = configurationService.getContextForView();

        var expectedContext = {
            textMechanism: {
                active: true,
                hint: 'Enter your feedback',
                currentLength: 0,
                maxLength: 100,
                maxLengthVisible: 1,
                textareaStyle: 'color: #000000;',
                labelStyle: 'text-align: left; color: #00ff00; font-size: 13px;'
            },
            ratingMechanism: {
                active: true,
                title: 'Rate your user experience'
            }
        };

        expect(context).toEqual(expectedContext);
    });

    it('should return a css style string', () => {
        var textMechanism = configurationService.getMechanismConfig(textType);

        var cssStyle = configurationService.getCssStyle(textMechanism, [new ParameterPropertyPair('textareaFontColor', 'color')]);
        expect(cssStyle).toEqual('color: #000000;');

        var cssStyle2 = configurationService.getCssStyle(textMechanism,
            [new ParameterPropertyPair('textareaFontColor', 'color'), new ParameterPropertyPair('fieldFontType', 'font-style')]);
        expect(cssStyle2).toEqual('color: #000000; font-style: italic;');
    })
});
