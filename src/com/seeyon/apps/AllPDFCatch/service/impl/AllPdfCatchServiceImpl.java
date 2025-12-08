package com.seeyon.apps.AllPDFCatch.service.impl;

import com.seeyon.apps.AllPDFCatch.dao.AllPdfCatchDao;
import com.seeyon.apps.AllPDFCatch.service.AllPdfCatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AllPdfCatchServiceImpl implements AllPdfCatchService {
    @Autowired
    private AllPdfCatchDao allPdfCatchDao;

    public AllPdfCatchServiceImpl() {
    }
}
