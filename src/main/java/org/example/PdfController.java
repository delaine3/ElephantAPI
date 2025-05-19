package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PdfController {

    @Autowired
    private PdfGenerator pdfGenerator;

    @Autowired
    private ElephantRepository elephantRepository;


}
