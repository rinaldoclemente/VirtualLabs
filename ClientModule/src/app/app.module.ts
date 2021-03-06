import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatIconModule} from '@angular/material/icon';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatListModule} from '@angular/material/list';
import {MatTabsModule} from '@angular/material/tabs';
import {MatTableModule} from '@angular/material/table';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatButtonModule} from '@angular/material/button';
import {MatInputModule} from '@angular/material/input';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatSortModule} from '@angular/material/sort';
import {MatPaginatorModule} from '@angular/material/paginator';
import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {StudentsComponent} from './components/tabs/students/students.component';
import {RouterModule} from '@angular/router';
import {routes} from './app-routing-module';
import {HomeComponent} from './components/main/home/home.component';
import {PersonalComponent} from './components/main/personal/personal.component';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {MatDialogModule} from '@angular/material/dialog';
import {LoginDialogComponent} from './helpers/dialog/login-dialog.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {AuthInterceptorService} from './auth/auth-interceptor.service';
import {VmComponent} from './components/tabs/vms/vm.component';
import {LabComponent} from './components/tabs/labs/lab.component';
import {TeamComponent} from './components/tabs/teams/team.component';
import {CourseInfoComponent} from './components/tabs/info/course-info.component';
import {SignupDialogComponent} from './helpers/dialog/signup-dialog.component';
import {PageNotFoundComponent} from './components/main/page_not_found/page-not-found.component';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MySnackBarComponent} from './helpers/my-snack-bar.component';
import {MatCardModule} from '@angular/material/card';
import {MatSelectModule} from '@angular/material/select';
import {MatSliderModule} from '@angular/material/slider';
import {VmModelSettingsDialogComponent} from './helpers/dialog/vm-model-settings-dialog.component';
import {AreYouSureDialogComponent} from './helpers/dialog/are-you-sure-dialog.component';
import {EmailDialogComponent} from './helpers/dialog/email-dialog.component';
import {VersionDialogComponent} from './helpers/dialog/version-dialog.component';
import {GradeDialogComponent} from './helpers/dialog/grade-dialog.component';
import {MatChipsModule} from '@angular/material/chips';
import {ExtendedModule, FlexLayoutModule, FlexModule} from '@angular/flex-layout';
import {VirtualDesktopComponent} from './components/vm_viewer/virtual-desktop.component';
import {VmInfoDialogComponent} from './helpers/dialog/vm-info-dialog.component';
import {GradeNumberDirective} from './helpers/grade-number.directive';
import {VarDirective} from './helpers/ng-var.directive';
import {AssignmentDialogComponent} from './helpers/dialog/assignment-dialog.component';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatNativeDateModule} from '@angular/material/core';
import {MatMomentDateModule} from '@angular/material-moment-adapter';
import {CanvasComponent} from './helpers/canvas.component';
import {ColorCircleModule} from 'ngx-color/circle';
import {MatTooltipModule} from '@angular/material/tooltip';
import {AddVersionDialogComponent} from './helpers/dialog/add-version-dialog.component';
import {TeamProposalDialogComponent} from './helpers/dialog/team-proposal-dialog.component';
import {ViewTeamProposalDialogComponent} from './helpers/dialog/view-team-proposal-dialog.component';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {ScrollingModule} from '@angular/cdk/scrolling';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {ProposalResponseComponent} from './components/main/proposal_response/proposal-response.component';
import {MatRadioModule} from '@angular/material/radio';
import {VmSettingsDialogComponent} from './helpers/dialog/vm-settings-dialog.component';
import {MatMenuModule} from '@angular/material/menu';
import {CourseDialogComponent} from './helpers/dialog/course-dialog.component';
import {MatStepperModule} from '@angular/material/stepper';
import {RegistrationConfirmComponent} from './components/main/registration_confirm/registration-confirm.component';
import {CsvImportDialogComponent} from './helpers/dialog/csv-import-dialog.component';

@NgModule({
  declarations: [
    AppComponent,
    PersonalComponent,
    StudentsComponent,
    HomeComponent,
    LoginDialogComponent,
    VmComponent,
    LabComponent,
    TeamComponent,
    CourseInfoComponent,
    SignupDialogComponent,
    PageNotFoundComponent,
    MySnackBarComponent,
    VmModelSettingsDialogComponent,
    AreYouSureDialogComponent,
    EmailDialogComponent,
    VirtualDesktopComponent,
    EmailDialogComponent,
    VersionDialogComponent,
    GradeDialogComponent,
    TeamProposalDialogComponent,
    ViewTeamProposalDialogComponent,
    AddVersionDialogComponent,
    VmInfoDialogComponent,
    AssignmentDialogComponent,
    GradeNumberDirective,
    VarDirective,
    CanvasComponent,
    ProposalResponseComponent,
    VmSettingsDialogComponent,
    CourseDialogComponent,
    RegistrationConfirmComponent,
    CsvImportDialogComponent
  ],
  imports: [
    RouterModule.forRoot(routes, {enableTracing: false}),
    BrowserModule,
    BrowserAnimationsModule,
    MatToolbarModule,
    MatIconModule,
    MatSidenavModule,
    MatListModule,
    MatTabsModule,
    MatTableModule,
    MatCheckboxModule,
    MatButtonModule,
    MatInputModule,
    MatAutocompleteModule,
    MatSortModule,
    MatPaginatorModule,
    HttpClientModule,
    MatDialogModule,
    FormsModule,
    ReactiveFormsModule,
    MatExpansionModule,
    MatSnackBarModule,
    MatCardModule,
    MatSelectModule,
    MatSliderModule,
    MatChipsModule,
    FlexLayoutModule,
    FlexModule,
    ExtendedModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatMomentDateModule,
    ColorCircleModule,
    MatTooltipModule,
    DragDropModule,
    ScrollingModule,
    MatProgressBarModule,
    MatRadioModule,
    MatMenuModule,
    MatStepperModule,
  ],
  exports: [
    RouterModule,
  ],
  providers: [
    MatDatepickerModule,
    MatNativeDateModule,
    MySnackBarComponent,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptorService,
      multi: true
    }
  ],
  bootstrap: [AppComponent],
  entryComponents: [
    AreYouSureDialogComponent,
    LoginDialogComponent,
    SignupDialogComponent,
    EmailDialogComponent,
    VersionDialogComponent,
    GradeDialogComponent,
    AddVersionDialogComponent,
    TeamProposalDialogComponent,
    ViewTeamProposalDialogComponent,
    VmModelSettingsDialogComponent,
    VmInfoDialogComponent,
    AssignmentDialogComponent,
    VmSettingsDialogComponent,
    CourseDialogComponent,
    CsvImportDialogComponent
  ]
})
export class AppModule { }
