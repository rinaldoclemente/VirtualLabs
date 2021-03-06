package it.polito.ai.virtualLabs.services;

import it.polito.ai.virtualLabs.dtos.*;
import it.polito.ai.virtualLabs.entities.*;
import it.polito.ai.virtualLabs.repositories.*;
import it.polito.ai.virtualLabs.services.exceptions.assignment.AssignmentNotFoundException;
import it.polito.ai.virtualLabs.services.exceptions.course.CourseNotFoundException;
import it.polito.ai.virtualLabs.services.exceptions.professor.ProfessorNotFoundException;
import it.polito.ai.virtualLabs.services.exceptions.report.ReportNotFoundException;
import it.polito.ai.virtualLabs.services.exceptions.student.StudentNotFoundException;
import it.polito.ai.virtualLabs.services.exceptions.version.VersionNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@Transactional
public class LabServiceImpl implements LabService {

    private static final String VERSION_CONTENT_PATH = "home/files/images/lab/versions/";
    private static final String VERSION_CONTENT_FORMAT = "png";
    private static final String REVIEW_IMAGE_PATH = "home/files/images/lab/reviews/";
    private static final String REVIEW_IMAGE_FORMAT = "png";

    @Autowired
    AssignmentRepository assignmentRepository;
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    VersionRepository versionRepository;
    @Autowired
    AuthService authService;
    @Autowired
    ModelMapper modelMapper;

    @Override
    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    public Optional<ReportDTO> getReport(Long reportId) {
        if (!reportRepository.existsById(reportId))
            return Optional.empty();

        authService.checkAuthorizationForReport(reportId);

        return reportRepository.findById(reportId)
                .map(r -> modelMapper.map(r, ReportDTO.class));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    public Optional<AssignmentDTO> getAssignment(Long assignmentId) {
        Optional<Assignment> assignmentOpt = assignmentRepository.findById(assignmentId);
        if (!assignmentOpt.isPresent())
            return Optional.empty();

        authService.checkAuthorizationForCourse(assignmentOpt.get().getCourse().getName());

        return assignmentOpt.map(a -> modelMapper.map(a, AssignmentDTO.class));
    }

    @Override
    public Optional<VersionDTO> getVersion(Long versionId) {
        if (!versionRepository.existsById(versionId))
            return Optional.empty();
        return versionRepository.findById(versionId)
                .map(v -> modelMapper.map(v, VersionDTO.class));
    }

    @Override
    public List<AssignmentDTO> getAllAssignments() {
        return assignmentRepository.findAll()
                .stream()
                .map(a -> modelMapper.map(a, AssignmentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Optional<ReportDTO> getStudentReportForAssignment(String studentUsername, Long assignmentId) {
        if(!userRepository.studentExistsByUsername(studentUsername))
            throw new StudentNotFoundException("The student with username '" + studentUsername + "' does not exist");

        Optional<Assignment> assignmentOpt = assignmentRepository.findById(assignmentId);
        if(!assignmentOpt.isPresent())
            throw new AssignmentNotFoundException("The assignment with id " + assignmentId + " does not exist");

        authService.checkAuthorizationForCourse(assignmentOpt.get().getCourse().getName());

        Student s = userRepository.getStudentByUsername(studentUsername);
        return s.getReports()
                .stream()
                .filter(r -> r.getAssignment().getId().equals(assignmentId))
                .findFirst()
                .map(r -> modelMapper.map(r, ReportDTO.class));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    public List<ReportDTO> getAssignmentReports(Long assignmentId) {
        Optional<Assignment> assignmentOpt = assignmentRepository.findById(assignmentId);
        if(!assignmentOpt.isPresent())
            throw new AssignmentNotFoundException("The assignment with id " + assignmentId + " does not exist");

        Assignment a = assignmentOpt.get();
        authService.checkAuthorizationForCourse(a.getCourse().getName());

        return a.getReports()
                .stream()
                .map(r -> modelMapper.map(r, ReportDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProfessorDTO> getAssignmentProfessor(Long assignmentId) {
        if(!assignmentRepository.existsById(assignmentId))
            throw new AssignmentNotFoundException("The assignment with id " + assignmentId + " does not exist");

        Assignment a = assignmentRepository.getOne(assignmentId);
        return Optional.of(modelMapper.map(a.getProfessor(), ProfessorDTO.class));
    }

    @Override
    public List<VersionDTO> getReportVersions(Long reportId) {
        if(!reportRepository.existsById(reportId))
            throw new ReportNotFoundException("The report with id " + reportId + " does not exist");

        authService.checkAuthorizationForReport(reportId);

        Report r = reportRepository.getOne(reportId);
        return r.getVersions()
                .stream()
                .map(v -> modelMapper.map(v, VersionDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    public Optional<StudentDTO> getReportOwner(Long reportId) {
        if(!reportRepository.existsById(reportId))
            throw new ReportNotFoundException("The report with id " + reportId + " does not exist");

        authService.checkAuthorizationForReport(reportId);

        Report r = reportRepository.getOne(reportId);
        return Optional.of(modelMapper.map(r.getOwner(), StudentDTO.class));
    }

    @Override
    public List<AssignmentDTO> getCourseAssignments(String courseName) {
        if(!courseRepository.existsById(courseName))
            throw new CourseNotFoundException("The course named " + courseName + " does not exist");

        Course c = courseRepository.getOne(courseName);
        List<Assignment> assignments = c.getAssignments();

        assignments.forEach(assignment -> {
            if(assignment.getExpiryDate().isBefore(LocalDateTime.now())) {
                assignment.getReports().forEach(report -> {
                    if(report.getVersions().isEmpty()) {
                        report.setStatus(Report.ReportStatus.GRADED);
                        report.setStatusDate(LocalDateTime.now());
                        report.setGrade(0f);
                    } else {
                        if(report.getStatus() == Report.ReportStatus.NULL || report.getStatus() == Report.ReportStatus.READ) {
                            report.setStatus(Report.ReportStatus.SUBMITTED);
                            report.setStatusDate(LocalDateTime.now());
                        }
                    }
                });
            }
        });

        return c.getAssignments()
                .stream()
                .map(a -> modelMapper.map(a, AssignmentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    public Optional<ReportDTO> getReportForVersion(Long versionId) {
        Optional<Version> versionOpt = versionRepository.findById(versionId);
        if(!versionOpt.isPresent())
            throw new VersionNotFoundException("The version with id " + versionId + " does not exist");

        authService.checkAuthorizationForReport(versionOpt.get().getReport().getId());

        return Optional.of(modelMapper.map(versionRepository.getOne(versionId).getReport(), ReportDTO.class));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    public Optional<AssignmentDTO> getAssignmentForReport(Long reportId) {
        if(!reportRepository.existsById(reportId))
            throw new AssignmentNotFoundException("The report with id " + reportId + " does not exist");

        authService.checkAuthorizationForReport(reportId);

        return Optional.of(modelMapper.map(reportRepository.getOne(reportId).getAssignment(), AssignmentDTO.class));
    }

    @Override
    public Optional<CourseDTO> getAssignmentCourse(Long assignmentId) {
        if(!assignmentRepository.existsById(assignmentId))
            throw new AssignmentNotFoundException("The assignment with id " + assignmentId + " does not exist");

        return Optional.of(modelMapper.map(assignmentRepository.getOne(assignmentId).getCourse(), CourseDTO.class));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    public Long addAssignmentToCourse(AssignmentDTO assignmentDTO, String courseName, String professorId) {
        if(!courseRepository.existsById(courseName))
            throw new CourseNotFoundException("The course named " + courseName + " does not exist");
        if(!userRepository.professorExistsById(professorId))
            throw new ProfessorNotFoundException("The professor with id " + professorId + " does not exist");

        authService.checkAuthorizationForCourse(courseName);

        Course course = courseRepository.getOne(courseName);
        Professor professor = userRepository.getProfessorById(professorId);
        assignmentDTO.setReleaseDate(LocalDateTime.now());
        assignmentDTO.setExpiryDate(assignmentDTO.getExpiryDate());
        Assignment assignment = modelMapper.map(assignmentDTO, Assignment.class);

        //check if there is already an assignment with that name in that course
        if(course.getAssignments().stream().anyMatch(a -> a.getName().equals(assignmentDTO.getName())) ||
                course.getStudents().isEmpty())
            return 0L;

        assignmentRepository.saveAndFlush(assignment);

        course.getStudents().forEach(student -> {
            Report report = new Report();
            student.addReport(report);
            assignment.addReport(report);
        });

        //add assignment to course and professor
        assignment.setCourse(course);
        assignment.setProfessor(professor);

        return assignment.getId();
    }

    @Override
    public boolean addReportToAssignment(ReportDTO reportDTO, Long assignmentId, String studentId) {
        if(!assignmentRepository.existsById(assignmentId))
            throw new AssignmentNotFoundException("The assignment with id " + assignmentId + " does not exist");
        if(!userRepository.studentExistsById(studentId))
            throw new StudentNotFoundException("The student with id " + studentId + " does not exist");

        Assignment assignment = assignmentRepository.getOne(assignmentId);
        Student student = userRepository.getStudentById(studentId);
        Report report = modelMapper.map(reportDTO, Report.class);

        //check if there is already a report for that assignmentId and studentId
        if(reportRepository.findReportByAssignmentIdAndOwnerId(assignmentId, studentId).isPresent())
            return false;

        //add report to assignment and student
        report.setAssignment(assignment);
        report.setOwner(student);

        reportRepository.saveAndFlush(report);
        return true;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public boolean addVersionToReport(Long reportId, String title, MultipartFile inputFile) {
        if(!reportRepository.existsById(reportId))
            throw new ReportNotFoundException("The report with id " + reportId + " does not exist");

        authService.checkAuthorizationForReport(reportId);

        Report report = reportRepository.getOne(reportId);

        //if assignment is expired you cannot add a new version unless its revised or read
        if(report.getStatus() != Report.ReportStatus.REVISED && report.getStatus() != Report.ReportStatus.READ)
            return false;

        Version version = new Version();
        version.setTitle(title);

        String imageName = report.getOwner().getId() + "|" + System.currentTimeMillis();
        version.setContent(Base64.getEncoder().withoutPadding().encodeToString(imageName.getBytes()));

        try {
            File file = new File(VERSION_CONTENT_PATH + version.getContent() + "." + VERSION_CONTENT_FORMAT);
            file.createNewFile();
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(inputFile.getBytes());
            stream.close();
        } catch (IOException ex) {
            return false;
        }

        version.setReport(report);
        report.setStatus(Report.ReportStatus.SUBMITTED);
        report.setStatusDate(LocalDateTime.now());

        versionRepository.saveAndFlush(version);
        return true;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    public boolean removeAssignment(Long assignmentId) {
        if(!assignmentRepository.existsById(assignmentId))
            throw new AssignmentNotFoundException("The assignment with id " + assignmentId + " does not exist");

        authService.checkAuthorizationForReport(assignmentRepository.getOne(assignmentId).getReports().get(0).getId());

        //remove assignment
        assignmentRepository.deleteById(assignmentId);
        assignmentRepository.flush();
        return true;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    public boolean editAssignment(Long assignmentId, AssignmentDTO assignmentDTO) {
        if(!assignmentRepository.existsById(assignmentId))
            throw new AssignmentNotFoundException("The assignment with id " + assignmentId + " does not exist");

        //check date and name constraints
        Assignment assignment = assignmentRepository.getOne(assignmentId);

        authService.checkAuthorizationForReport(assignment.getReports().get(0).getId());

        if(assignmentDTO.getExpiryDate().isBefore(LocalDateTime.now()) ||
                (!assignment.getName().equals(assignmentDTO.getName()) &&
                assignment.getCourse().getAssignments().stream().anyMatch(a -> a.getName().equals(assignmentDTO.getName()))))
            return false;

        //edit assignment
        assignment.setName(assignmentDTO.getName());
        assignment.setContent(assignmentDTO.getContent());
        assignment.setExpiryDate(assignmentDTO.getExpiryDate());

        assignmentRepository.saveAndFlush(assignment);

        return true;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    public boolean gradeReport(Long reportId, Float grade) {
        if(!reportRepository.existsById(reportId))
            throw new ReportNotFoundException("The report with id " + reportId + " does not exist");

        authService.checkAuthorizationForReport(reportId);

        //check if grade is >= 0 and <= 30
        if(grade < 0 || grade > 30)
            return false;

        //check if there is already a grade for that report
        Report report = reportRepository.getOne(reportId);

        //the assignment must be expired and the report must be submitted
        if(report.getAssignment().getExpiryDate().isAfter(LocalDateTime.now()) && report.getStatus() != Report.ReportStatus.SUBMITTED)
            return false;

        //grade report
        report.setGrade(grade);
        report.setStatus(Report.ReportStatus.GRADED);
        report.setStatusDate(LocalDateTime.now());

        reportRepository.saveAndFlush(report);
        return true;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_PROFESSOR')")
    public boolean reviewVersion(Long versionId, String review) {
        if(!versionRepository.existsById(versionId))
            throw new VersionNotFoundException("The version with id " + versionId + " does not exist");

        Version version = versionRepository.getOne(versionId);
        Report report = version.getReport();

        authService.checkAuthorizationForReport(report.getId());

        //check that the assignment is expired
        if(report.getAssignment().getExpiryDate().isAfter(LocalDateTime.now()))
            return false;

        //report status must be submitted in order to be revised
        if(report.getStatus() != Report.ReportStatus.SUBMITTED)
            return false;

        //check if this version is last version submitted by the student
        AtomicBoolean isLast = new AtomicBoolean(true);
        report.getVersions().forEach(ver -> {
            if (ver.getSubmissionDate().isAfter(version.getSubmissionDate()))
                isLast.set(false);
        });
        if(!isLast.get())
            return false;


        byte[] image = Base64.getDecoder().decode(review);

        try {
            File file = new File(REVIEW_IMAGE_PATH + version.getContent() + "." + REVIEW_IMAGE_FORMAT);
            file.createNewFile();
            FileOutputStream fileStream = new FileOutputStream(file, false);

            fileStream.write(image);
            fileStream.close();
        } catch (IOException ex) {
            return false;
        }

        version.setRevised(true);
        version.getReport().setStatus(Report.ReportStatus.REVISED);
        version.getReport().setStatusDate(LocalDateTime.now());
        versionRepository.saveAndFlush(version);

        return true;

    }

    @Override
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public boolean markReportAsRead(Long reportId) {
        if(!reportRepository.existsById(reportId))
            throw new ReportNotFoundException("The report with id " + reportId + " does not exist");

        authService.checkAuthorizationForReport(reportId);

        Report report = this.reportRepository.getOne(reportId);
        if(report.getStatus() != Report.ReportStatus.NULL)
            return false;

        report.setStatus(Report.ReportStatus.READ);
        report.setStatusDate(LocalDateTime.now());

        reportRepository.saveAndFlush(report);

        return true;
    }
}
