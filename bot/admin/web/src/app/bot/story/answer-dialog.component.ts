import {Component, EventEmitter, Inject, OnInit} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef, MatSnackBar} from "@angular/material";
import {StateService} from "../../core-nlp/state.service";
import {AnswerConfiguration, AnswerConfigurationType, AnswerContainer} from "../model/story";
import {BotService} from "../bot-service";

@Component({
  selector: 'tock-answer-dialog',
  templateUrl: './answer-dialog.component.html',
  styleUrls: ['./answer-dialog.component.css']
})
export class AnswerDialogComponent implements OnInit {

  create: boolean;
  answer: AnswerContainer;
  answerLabel: string = "Answer";

  submit: EventEmitter<boolean> = new EventEmitter<boolean>();

  private originalCurrentType: AnswerConfigurationType;
  private originalAnswers: AnswerConfiguration[];

  private currentSave = false;

  constructor(
    public dialogRef: MatDialogRef<AnswerDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private state: StateService,
    private bot: BotService,
    private snackBar: MatSnackBar) {
    this.create = this.data.create;
    this.answer = this.data.answer;
    this.answerLabel = this.data.answerLabel ? this.data.answerLabel : "Answer";
    this.originalCurrentType = this.answer.currentType;
    this.originalAnswers = this.answer.answers.slice(0).map(a => a.clone());
  }

  ngOnInit() {
    this.submit.subscribe(_ => this.save())
  }

  save() {
    if (!this.currentSave) {
      this.currentSave = true;
      this.submit.emit(true);
      let invalidMessage = this.answer.currentAnswer().invalidMessage();
      if (invalidMessage) {
        this.currentSave = false;
        this.snackBar.open(`Error: ${invalidMessage}`, "ERROR", {duration: 5000});
      } else if (!this.create) {
        this.answer.save(this.bot).subscribe(r => {
          this.dialogRef.close({
            answer: this.answer
          });
          this.snackBar.open(`${this.answerLabel} Modified`, "UPDATE", {duration: 1000});
        });
      } else {
        this.dialogRef.close({
          answer: this.answer
        });
      }
    }

  }

  cancel() {
    this.answer.currentType = this.originalCurrentType;
    this.answer.answers = this.originalAnswers;
    this.answer.answers.forEach(a => a.checkAfterReset(this.bot));
    this.dialogRef.close({});
  }

}
