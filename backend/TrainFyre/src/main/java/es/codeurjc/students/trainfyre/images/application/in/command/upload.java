package es.codeurjc.students.trainfyre.images.application.in.command;

import org.jmolecules.architecture.cqrs.Command;

import java.io.InputStream;

import java.util.UUID;

@Command
public record upload(UUID uuid, InputStream content, long contentLength, String contentType) { }
