import {AttachmentMechanism} from '../../models/mechanisms/attachment_mechanism';
import {feedbackApp} from '../../js/jquery.feedback';
import {AttachmentFeedback} from '../../models/feedbacks/attachment_feedback';
import {MechanismView} from '../mechanism_view';


export class AttachmentView implements MechanismView {
    dropArea:any;

    constructor(private attachmentMechanism:AttachmentMechanism, private dialogId:string) {
        if (attachmentMechanism.active) {
            this.dropArea = jQuery('' + this.getSelector()).find('.drop-area');
            this.dropArea.fileUpload(feedbackApp.options.distPath);
        }
    }

    getSelector():string {
        return "#" + this.dialogId + " #attachmentMechanism" + this.attachmentMechanism.id;
    }

    getFiles() {
        return this.dropArea.currentFiles;
    }

    getFeedbacks():AttachmentFeedback[] {
        let attachmentFeedbacks = [];

        for (var i = 0; i < this.getFiles().length; i++) {
            var file = this.getFiles()[i];
            let partName = 'attachment' + i;
            var attachmentFeedback = new AttachmentFeedback(partName, file.name, file.type, this.attachmentMechanism.id);
            attachmentFeedbacks.push(attachmentFeedback);
        }

        return attachmentFeedbacks;
    }

    getPartName(index:number):string {
        return 'attachment' + index;
    }

    reset() {
        // TODO implement
    }
}