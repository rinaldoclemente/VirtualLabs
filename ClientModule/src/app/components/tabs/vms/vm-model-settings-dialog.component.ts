import {ChangeDetectorRef, Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {VmModel} from '../../../models/vm-model.model';

@Component({
  selector: 'app-vm-model-settings-dialog',
  templateUrl: './vm-model-settings-dialog.component.html',
  styleUrls: ['./vm-model-settings-dialog.component.css', './vm.component.css']
})
export class VmModelSettingsDialogComponent implements OnInit {

  public vmModelFormGroup: FormGroup;
  public formIsInvalid = false;

  constructor(public dialogRef: MatDialogRef<VmModelSettingsDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private cd: ChangeDetectorRef,
              public formBuilder: FormBuilder) {


  }

  ngOnInit(): void {
    this.vmModelFormGroup = this.formBuilder.group({
      name: ['', Validators.required],
      os: ['', Validators.required],
      maxCpu: [0, Validators.min(1)],
      maxRam: [0, Validators.min(1)],
      maxDisk: [0, Validators.min(1)],
      maxNumVms: [0, Validators.min(1)],
      maxActiveVms: [0, Validators.min(1)]
    });

    if (this.data.modelExists) {
      const vmModel: VmModel = this.data.vmModel;

      this.vmModelFormGroup.get('name').setValue(vmModel.name);
      this.vmModelFormGroup.get('os').setValue(vmModel.os);
      this.vmModelFormGroup.get('maxCpu').setValue(vmModel.maxVCPU);
      this.vmModelFormGroup.get('maxRam').setValue(vmModel.maxRAM);
      this.vmModelFormGroup.get('maxDisk').setValue(vmModel.maxDisk);
      this.vmModelFormGroup.get('maxNumVms').setValue(vmModel.maxTotVm);
      this.vmModelFormGroup.get('maxActiveVms').setValue(vmModel.maxActiveVm);

      this.cd.detectChanges();
    }
  }

  calcDiskLabel(value: number) {
    if (value < 1024)
      return value + ' GB';
    else if (value % 1024)
      return (value / 1024).toFixed(1) + ' TB';
    else
      return (value / 1024) + ' TB';
  }

  checkForm() {

    if (this.vmModelFormGroup.invalid) {
      this.formIsInvalid = true;
      return;
    }

    const vmModel = new VmModel(null,
      this.vmModelFormGroup.get('name').value,
      this.vmModelFormGroup.get('os').value,
      this.vmModelFormGroup.get('maxCpu').value,
      this.vmModelFormGroup.get('maxRam').value,
      this.vmModelFormGroup.get('maxDisk').value,
      this.vmModelFormGroup.get('maxNumVms').value,
      this.vmModelFormGroup.get('maxActiveVms').value);

    this.dialogRef.close(vmModel);
  }
}
