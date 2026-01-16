package com.rathaur.nexus.portfolioservice.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rathaur.nexus.portfolioservice.entity.Project;
import com.rathaur.nexus.portfolioservice.repository.ProjectRepository;


@RestController()
@RequestMapping("/api/portfolio")
public class ProjectController {
    
    private final ProjectRepository projectRepository;

    public ProjectController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @GetMapping("/projects")
    public List<Project> findAllProjects(){
        return projectRepository.findAll();
    }

    @PostMapping("/projects")
    public Project createProject(@RequestBody Project project){
        return projectRepository.save(project);
    }
}
