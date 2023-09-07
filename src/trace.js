import AWSXRay from "aws-xray-sdk-core";

export const captureAWSv3Client = (client) => {
    return AWSXRay.captureAWSv3Client(client);
};

export const getSegment = () => {
    return AWSXRay.getSegment();
};

export const beginSubsegment = (segment, name) => {
    return segment.addNewSubsegment(name);
};

export const endSubsegment = (subsegment) => {
    return subsegment.close();
};

