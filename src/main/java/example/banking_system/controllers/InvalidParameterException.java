package example.banking_system.controllers;

//TODO consider making it runtime and get rid of unnecessary throws
// in services we make checked only those exception which we might recover from
// DONE
public class InvalidParameterException extends RuntimeException{
}
